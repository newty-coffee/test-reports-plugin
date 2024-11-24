/*
 * Copyright 2024 newty.coffee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.newtco.test.reports.plugin.test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.newtco.test.reports.api.test.model.*;
import com.newtco.test.reports.plugin.PluginVersion;
import com.newtco.test.util.FilterSet;
import com.newtco.test.util.GitLinkTemplate;
import com.newtco.test.util.SourceSetCollectors;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.*;
import org.gradle.testing.base.TestingExtension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.newtco.test.util.Gradle.Extensions.extensionOf;

public class TestSuiteCollector implements TestListener, TestOutputListener {

    private final AtomicInteger                  idGenerator;
    private final Map<String, TestSuite>         nodes;
    private final Map<TestDescriptor, StdOutput> outputs;
    private final FilterSet                      stackFilterSet;
    private final Provider<String>               repository;
    private final Provider<String>               commit;
    private final Provider<String>               urlTemplate;
//    private final UrlTemplate.UrlBuilder         urlBuilder;


    public TestSuiteCollector(FilterSet stackFilterSet, Provider<String> repository, Provider<String> commit, Provider<String> urlTemplate) {
        this.idGenerator    = new AtomicInteger(0);
        this.nodes          = new ConcurrentHashMap<>();
        this.outputs        = new ConcurrentHashMap<>();
        this.stackFilterSet = stackFilterSet;
        this.repository     = repository;
        this.commit         = commit;
        this.urlTemplate    = urlTemplate;
    }

    @Override
    public void beforeSuite(TestDescriptor descriptor) {
        // Ignore Gradle wrapper suites
        if (!isGradleSuite(descriptor.getName())) {
            nodes.computeIfAbsent(nodeKeyOf(descriptor), unused -> createTestSuite(descriptor));
            outputs.computeIfAbsent(descriptor, unused -> new StdOutput());
        }
    }

    @Override
    public void afterSuite(TestDescriptor descriptor, TestResult result) {
        var suite  = nodes.get(nodeKeyOf(descriptor));
        var output = outputs.remove(descriptor);
        if (suite != null) {
            updateTestSuite(suite, output, result);
        }
    }

    @Override
    public void beforeTest(TestDescriptor descriptor) {
        outputs.put(descriptor, new StdOutput());
    }

    @Override
    public void afterTest(TestDescriptor descriptor, TestResult result) {
        var output = outputs.remove(descriptor);
        var suite  = getTestSuite(descriptor);
        if (suite != null) {
            addOrUpdateTestCase(suite, descriptor, result, output);
        }
    }

    @Override
    public void onOutput(TestDescriptor descriptor, TestOutputEvent event) {
        var output = outputs.get(descriptor);
        if (output != null) {
            if (event.getDestination() == TestOutputEvent.Destination.StdErr) {
                output.err.append(event.getMessage());
            } else {
                output.out.append(event.getMessage());
            }
        }
    }

    @Nonnull
    public List<TestSuite> getTestSuites(Test test) {
        var urlBuilder = GitLinkTemplate.createLinkBuilder(urlTemplate.get());

        var values = new ArrayList<>(nodes.values());
        finalizeTestSuites(test, values, urlBuilder);
        values.sort(Comparator.comparing(TestSuite::getStatus));

        return values;
    }

    private String nodeKeyOf(TestDescriptor descriptor) {
        // Nearly all descriptors will have a class name set, and since this collects per test task the
        // names should be unique
        var className = descriptor.getClassName();
        return className != null ? className : String.valueOf(System.identityHashCode(descriptor));
    }

    @Nullable
    private TestSuite getTestSuite(TestDescriptor descriptor) {
        var suite = nodes.get(nodeKeyOf(descriptor));
        while (suite == null && descriptor.getParent() != null) {
            descriptor = descriptor.getParent();
            suite      = nodes.get(nodeKeyOf(descriptor));
        }
        return suite;
    }

    @Nullable
    private TestCase getTestCase(TestSuite suite, TestDescriptor descriptor) {
        for (var test : suite.tests) {
            if (Objects.equals(test.name, descriptor.getName())
                    && Objects.equals(test.className, descriptor.getClassName())) {
                return test;
            }
        }

        return null;
    }

    private void finalizeTestSuites(Test test, Collection<TestSuite> suites, GitLinkTemplate.GitLinkBuilder gitLinkBuilder) {
        var sourceFiles = getTestSourceFiles(suites, test);
        var stackFilter = stackFilterSet.asPredicate();
        for (var suite : suites) {
            finalizeTestCases(test.getProject().getRootDir().toPath(), suite.tests, sourceFiles, stackFilter, gitLinkBuilder);
            suite.tests.sort(Comparator.comparing(TestCase::getStatus));
        }
    }

    private void finalizeTestCases(
            Path rootDir,
            List<TestCase> testsCases,
            Map<String, Path> sourceFiles,
            Predicate<String> stackFilter,
            GitLinkTemplate.GitLinkBuilder gitLinkBuilder) {

        for (var testcase : testsCases) {
            var sourceFile = sourceFiles.get(testcase.getOuterClassName());

            if (sourceFile != null) {
                testcase.sourceFile = rootDir.relativize(sourceFile).toString()
                        .replace('\\', '/');
                testcase.url        = getUrl(gitLinkBuilder, testcase.sourceFile);
            }

            for (var failure : testcase.failures) {
                failure.lineNumber = LineNumberResolver.getFailureLineNumber(
                        testcase.className,
                        testcase.name,
                        failure.stackTrace,
                        sourceFile
                );

                failure.stackTrace = filterStackTrace(failure.stackTrace, stackFilter);
            }
        }
    }

    /**
     * Collects and returns a map of fully qualified class names associated with their respective source file paths from
     * the provided test suites and test.
     *
     * @param suites the list of TestSuite objects containing the test cases
     * @param test   the Test object that provides additional context for retrieving source sets
     * @return a map where the key is the fully qualified class name, and the value is the relative path of the source
     * file from the project root directory.
     */
    private Map<String, Path> getTestSourceFiles(Collection<TestSuite> suites, Test test) {
        var testClassNames = getTestCaseClassNames(suites);
        if (testClassNames.isEmpty()) {
            return Map.of();
        }

        var sourceSets = getTestSourceSets(test);

        return SourceSetCollectors.sourcesMatching(
                sourceSets,
                (className, element) -> testClassNames.contains(className),
                (className, element) -> element.getFile(),
                (fileMap) -> {
                    // Convert to className -> file paths
                    var converted = new HashMap<String, Path>();
                    for (var entry : fileMap.entrySet()) {
                        // If the SourceSets have overlapping files, this will take the last one
                        var previous = converted.put(entry.getValue(), entry.getKey().toPath());
                        if (previous != null) {
                            test.getLogger().warn("Duplicate SourceSet file \"{}\" detected for class {} from SourceSets {}. Line numbers of test failures may be inaccurate.",
                                    previous,
                                    entry.getValue(),
                                    sourceSets.stream().map(SourceSet::getName).collect(Collectors.joining(", ")));
                        }
                    }
                    return converted;
                });
    }

    /**
     * Retrieves a set of unique outer class names from the provided list of test suites.
     *
     * @param suites the list of TestSuite objects from which to extract the test case class names
     * @return a set of unique test case class names
     */
    private Set<String> getTestCaseClassNames(Collection<TestSuite> suites) {
        return suites.stream()
                .map(TestSuite::getTests)
                .flatMap(List::stream)
                .map(TestCase::getOuterClassName)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the source sets associated with the specified test task.
     *
     * @param test the test for which the source sets are to be retrieved
     * @return a list of source sets associated with the test
     */
    // meets minimum gradle version 7.6
    @SuppressWarnings("UnstableApiUsage")
    private List<SourceSet> getTestSourceSets(Test test) {
        var taskName   = test.getName();
        var sourceSets = new ArrayList<SourceSet>();

        for (var suite : extensionOf(test.getProject(), TestingExtension.class).getSuites()) {
            if (suite instanceof JvmTestSuite jvmSuite) {
                for (var target : jvmSuite.getTargets()) {
                    if (taskName.equals(target.getTestTask().getName())) {
                        sourceSets.add(jvmSuite.getSources());
                    }
                }
            }
        }

        return sourceSets;
    }

    private boolean isGradleSuite(String name) {
        return name.startsWith("Gradle Test Executor")
                || name.startsWith("Gradle Test Run");
    }

    private TestSuite createTestSuite(TestDescriptor descriptor) {
        var suite = new TestSuite();
        suite.id          = idGenerator.incrementAndGet();
        suite.name        = descriptor.getName();
        suite.displayName = descriptor.getDisplayName();
        suite.className   = descriptor.getClassName();
        suite.stdErr      = "";
        suite.stdOut      = "";
        suite.tests       = new ArrayList<>();

        return suite;
    }

    private void updateTestSuite(TestSuite suite, StdOutput output, TestResult result) {
        suite.status = getStatus(result.getResultType());
        if (output != null) {
            suite.stdErr += output.err.toString();
            suite.stdOut += output.out.toString();
        }
        collectMetrics(suite, result);
    }

    private Status getStatus(TestResult.ResultType resultType) {
        return switch (resultType) {
            case SUCCESS -> Status.PASSED;
            case FAILURE -> Status.FAILED;
            case SKIPPED -> Status.SKIPPED;
        };
    }

    private void collectMetrics(Stats stats, TestResult result) {
        stats.total     = stats.total + result.getTestCount();
        stats.passed    = stats.passed + result.getSuccessfulTestCount();
        stats.skipped   = stats.skipped + result.getSkippedTestCount();
        stats.failed    = stats.failed + result.getFailedTestCount();
        stats.startTime = stats.startTime > 0
                ? Math.min(stats.startTime, result.getStartTime())
                : result.getStartTime();
        stats.endTime   = Math.max(stats.endTime, result.getEndTime());
        stats.duration  = stats.endTime - stats.startTime;
    }

    private void addOrUpdateTestCase(TestSuite suite, TestDescriptor descriptor, TestResult result, StdOutput output) {
        var existing = getTestCase(suite, descriptor);
        if (existing != null) {
            updateTestCase(existing, descriptor, result, output);
        } else {
            suite.tests.add(createTestCase(descriptor, result, output));
        }
    }

    // meets minimum gradle version 7.6
    @SuppressWarnings("UnstableApiUsage")
    private void updateTestCase(TestCase test, TestDescriptor descriptor, TestResult result, StdOutput output) {
        // Only change the outcome if the test hasn't passed yet. This occurs when retries are done. Once a test has
        // passed, the only thing collected are outputs and failures
        if (test.status != Status.PASSED) {
            test.status = getStatus(result.getResultType());
        }
        if (output != null) {
            test.stdErr += output.err.toString();
            test.stdOut += output.out.toString();
        }

        test.failures.addAll(createFailures(result.getFailures()));
    }

    // meets minimum gradle version 7.6
    @SuppressWarnings("UnstableApiUsage")
    private TestCase createTestCase(TestDescriptor descriptor, TestResult result, StdOutput output) {
        var test = new TestCase();
        test.id          = idGenerator.incrementAndGet();
        test.name        = descriptor.getName();
        test.displayName = descriptor.getDisplayName();
        test.className   = descriptor.getClassName();
        test.sourceFile  = ""; // Assigned in post-processing
        test.url         = ""; // Assigned in post-processing
        test.status      = getStatus(result.getResultType());
        if (output != null) {
            test.stdErr = output.err.toString();
            test.stdOut = output.out.toString();
        } else {
            test.stdErr = "";
            test.stdOut = "";
        }
        test.failures = createFailures(result.getFailures());

        collectMetrics(test, result);

        return test;
    }

    // meets minimum gradle version 7.6
    @SuppressWarnings("UnstableApiUsage")
    private List<Failure> createFailures(List<TestFailure> testFailures) {
        var failures = new ArrayList<Failure>();

        for (var failure : testFailures) {
            failures.add(createFailure(failure));
        }

        return failures;
    }

    // meets minimum gradle version 7.6
    @SuppressWarnings("UnstableApiUsage")
    private Failure createFailure(TestFailure testFailure) {
        var details = testFailure.getDetails();

        var failure = switch (getFailureType(details)) {
            case EXCEPTION -> {
                var exception = new Failure();
                exception.type = FailureType.EXCEPTION;
                yield exception;
            }
            case ASSERTION -> {
                var assertion = new AssertionFailure();
                assertion.type     = FailureType.ASSERTION;
                assertion.expected = details.getExpected();
                assertion.actual   = details.getActual();
                yield assertion;
            }
            case FILE -> {
                // Should not happen with gradle < 8.3
                if (!PluginVersion.isGradleVersionAtLeast("8.3")) {
                    throw new IllegalStateException("Unsupported use of Gradle 8.3 features in plugin. Please upgrade to Gradle 8.3 or later or contact the developers of this plugin to report this issue.");
                }

                var file = new FileFailure();
                file.type         = FailureType.FILE;
                file.expectedPath = details.getExpected();
                file.expectedData = details.getExpectedContent();
                file.actualPath   = details.getActual();
                file.actualData   = details.getActualContent();
                yield file;
            }
        };

        failure.message    = details.getMessage();
        failure.className  = details.getClassName();
        failure.stackTrace = details.getStacktrace();
        failure.lineNumber = -1; // Assigned in post-processing
        failure.causes     = createFailures(testFailure.getCauses());

        return failure;
    }

    private List<String> splitStackTrace(String stackTrace) {
        var lines = new ArrayList<String>();

        var current = new StringBuilder();
        int length  = stackTrace.length();

        // Find the first \tat line
        int i = stackTrace.indexOf(System.lineSeparator() + "\tat");
        if (i != -1) {
            lines.add(stackTrace.substring(0, i));
            i += System.lineSeparator().length();
        } else {
            i = 0;
        }

        while (i < length) {
            int j = i; // start of the next line
            int k = i; // end of the next line

            while (i < length) {
                char ch = stackTrace.charAt(i);
                if (ch == '\r') {
                    k = i++;
                    if (i < length && stackTrace.charAt(i) == '\n') {
                        i++;
                    }
                    break;
                } else if (ch == '\n') {
                    k = i++;
                    break;
                } else {
                    i++;
                }
            }

            var line = stackTrace.substring(j, k);
            if (line.startsWith("\tat")) {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                lines.add(line);
            } else {
                current.append(line);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return lines;
    }

    protected String filterStackTrace(String stackTrace, Predicate<String> stackFilter) {
        var filtered = new StringBuilder();
        int more     = 0;
        int include  = 0;

        for (var line : splitStackTrace(stackTrace)) {
            if (include-- > 0) {
                filtered.append(line).append('\n');
            } else if (line.startsWith("\tat ")) {
                if (stackFilter.test(line.substring(4))) {
                    if (more > 0) {
                        filtered.append("\t... ").append(more).append(" more").append('\n');
                    }
                    filtered.append(line).append('\n');
                    more    = 0;
                    include = 1;
                } else {
                    more += 1;
                }
            } else {
                if (more > 0) {
                    filtered.append("\t... ").append(more).append(" more").append('\n');
                }
                filtered.append(line).append('\n');
                include = 1;
            }
        }

        // Append any leftover
        if (more > 0) {
            filtered.append("\t... ").append(more).append(" more");
        } else if (filtered.charAt(filtered.length() - 1) == '\n') {
            filtered.setLength(filtered.length() - 1);
        }

        return filtered.toString();
    }

    // meets minimum gradle version 7.6
    @SuppressWarnings("UnstableApiUsage")
    private FailureType getFailureType(TestFailureDetails details) {
        if (PluginVersion.isGradleVersionAtLeast("8.3")) {
            if (details.isFileComparisonFailure()) {
                return FailureType.FILE;
            }
        }

        if (details.isAssertionFailure()) {
            return FailureType.ASSERTION;
        } else {
            return FailureType.EXCEPTION;
        }
    }

    private String getUrl(GitLinkTemplate.GitLinkBuilder gitLinkBuilder, String file) {
        if (repository.isPresent() && commit.isPresent()) {
            return gitLinkBuilder.build(repository.get(), commit.get(), file);
        }
        return "";
    }


    private static class LineNumberResolver {

        private static final JavaParser PARSER = new JavaParser(new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.RAW));

        public static int getFailureLineNumber(String className, String methodName, String stackTrace,
                                               Path sourceFile) {
            if (methodName.endsWith("()")) {
                methodName = methodName.substring(0, methodName.length() - 2);
            }

            int line = -1;

            if (!stackTrace.isEmpty()) {
                line = getLineNumberFromStackTrace(className + "." + methodName, stackTrace);
            }

            if (line == -1 && sourceFile != null) {
                line = getLineNumberFromSourceFile(methodName, sourceFile);
            }

            return line;
        }

        private static int getLineNumberFromStackTrace(String className, String stackTrace) {
            int start = stackTrace.indexOf(className);
            if (start != -1) {
                // Next we have (filename.java:X)
                start = stackTrace.indexOf(":", start);
                if (start != -1) {
                    int term = stackTrace.indexOf(")", start + 1);
                    if (term != -1) {
                        return Integer.parseInt(stackTrace, start + 1, term, 10);
                    }
                }
            }
            return -1;
        }

        private static int getLineNumberFromSourceFile(String methodName, Path sourceFile) {
            int line = -1;

            try {
                var source = Files.readString(sourceFile);

                // Do a quick check for the method name appearing only once...
                int offset = source.indexOf(methodName);
                if (offset != -1 && offset == source.lastIndexOf(methodName)) {
                    line = getLineNumberOfOffset(source, offset);
                }

                if (line == -1) {
                    line = getLineNumberFromParser(source, methodName);
                }
            } catch (IOException e) {
                // Ignore
            }

            return line;
        }

        private static int getLineNumberOfOffset(String source, int offset) {
            int line = 1;
            for (int i = 0; i < offset; i++) {
                char currentChar = source.charAt(i);
                if (currentChar == '\n') {
                    line++;
                } else if (currentChar == '\r') {
                    line++;
                    // Handle Windows-style "\r\n" by skipping the next '\n' if present
                    if (i + 1 < offset && source.charAt(i + 1) == '\n') {
                        i++; // Skip the '\n' as it's part of "\r\n"
                    }
                }
            }
            return line;
        }

        public static int getLineNumberFromParser(String source, String methodName) {
            var compilationUnit = PARSER.parse(source).getResult().orElse(null);
            if (null != compilationUnit) {
                var lines = compilationUnit.findAll(MethodDeclaration.class,
                                method -> method.getNameAsString().equals(methodName)
                                        && method.isAnnotationPresent("Test")).stream()
                        .map(method -> method.getBegin()
                                .map(p -> p.line)
                                .orElse(-1))
                        .toList();

                if (lines.size() == 1) {
                    return lines.get(0);
                }
            }

            return -1;
        }
    }

    private static class StdOutput {

        StringBuilder err;
        StringBuilder out;

        StdOutput() {
            err = new StringBuilder();
            out = new StringBuilder();
        }
    }
}
