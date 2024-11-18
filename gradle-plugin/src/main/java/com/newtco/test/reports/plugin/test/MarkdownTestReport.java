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

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.Test;

import com.newtco.test.reports.api.test.TestSettings;
import com.newtco.test.reports.api.test.model.TestSuite;
import com.newtco.test.templates.TemplateInstantiator;
import com.newtco.test.util.Text;


public class MarkdownTestReport extends TestReport {

    private final MarkdownReportSettings settings;
    private final TemplateInstantiator   instantiator;
    private final Project                project;
    private final MarkdownOptions        options;
    private final Logger                 logger;
    private final String                 name;

    public MarkdownTestReport(
        TemplateInstantiator instantiator,
        Test test,
        MarkdownReportSettings settings,
        DirectoryProperty outputDir) {
        this.settings     = settings;
        this.instantiator = instantiator;
        this.project      = test.getProject();
        this.options      = new MarkdownOptions(settings, outputDir);
        this.logger       = test.getLogger();
        this.name         = test.getName();
    }

    private String getTemplateName() {
        // Options name will be either Summary or Detailed
        return "tests." + options.getName() + "MarkdownReportTemplate";
    }

    @Override
    public void generateReport(List<TestSuite> suites) {
        logger.debug("Generating Markdown test report for {}:{}", project.getName(), name);

        var reportFile = options.resolveReportFile(name + "-" + options.getName());


        try (var writer = Files.newBufferedWriter(reportFile)) {
            var start = Instant.now();
            var template = instantiator.createTemplate(getTemplateName(),
                writer,
                project.getName() + ":" + name,
                accumulatedMetrics(suites),
                suites,
                getTemplateSettings());
            template.render();
            var end = Instant.now();

            logger.info("Finished generating Markdown summary results ({}) to: file:///{}",
                Text.Format.duration(start, end),
                reportFile.toString().replace('\\', '/'));

        } catch (Exception ex) {
            throw new GradleException("Failed to write Markdown test report", ex);
        }

    }

    /**
     * Generates the settings class used within a template
     */
    private TestSettings getTemplateSettings() {
        var markdown = new TestSettings();
        markdown.includeSystemErrLog = settings.getIncludeSystemErrLog().get();
        markdown.includeSystemOutLog = settings.getIncludeSystemOutLog().get();
        markdown.outputPerTestCase = settings.getOutputPerTestCase().get();
        markdown.statuses          = Set.copyOf(settings.getTestOutcomes().get());
        return markdown;
    }
//
//    private void run() {
//        var outputFile = settings.getReportFile().getAsFile().get().toPath();
//
//        try (var writer = Files.newBufferedWriter(outputFile)) {
//            var start = Instant.now();
//            var template = instantiator.createTemplate(getTemplateName(),
//                writer,
//                new Bundle(bundle),
//                new Badge(settings.getBadgeStyle().getOrElse("flat"),
//                    settings.getCurrentColorScheme().getColors()),
//                getTemplateSettings());
//            template.render();
//            var end = Instant.now();
//
//            log.lifecycle("Finished generating Markdown coverage report ({}) to: file:///{}",
//                Text.Format.duration(start, end),
//                outputFile.toString().replace('\\', '/'));
//        } catch (Exception e) {
//            throw new GradleException("Failed to write Markdown coverage report", e);
//        }
//    }


//    @Override
//    public void generateReport(List<TestGraph> suites) {
//        if (options.)
//            if (options.createSummary) {
//                createSummaryReport(suites);
//            }
//
//        createIndividualReports(suites);
//    }


//
//    private void createSummaryReport(List<TestGraph> suites) {
//        var reportFile = options.resolveReportFile(name + "-" + "summary");
//        try (var writer = Files.newBufferedWriter(reportFile)) {
//            var start = Instant.now();
//            new MarkdownReportGenerator(writer, options)
//                .generateSummaryReport(project.getName() + ":" + name, suites);
//            var end = Instant.now();
//
//            logger.info("Finished generating Markdown summary results ({}) to: {}",
//                Text.Format.duration(start, end),
//                reportFile);
//
//        } catch (IOException ex) {
//            logger.error("Failed to write report {}",
//                reportFile,
//                ex);
//        }
//    }
//
//    private void createIndividualReports(List<TestGraph> suites) {
//        for (var suite : suites) {
//            var suiteName  = suite.getDescriptor().getName();
//            var reportFile = options.resolveReportFile(suiteName);
//
//            try (var writer = Files.newBufferedWriter(reportFile)) {
//                var start = Instant.now();
//                new MarkdownReportGenerator(writer, options)
//                    .generateDetailedReport(suiteName, suite);
//
//                var end = Instant.now();
//                logger.info("Finished generating Markdown summary results ({}) to: {}",
//                    Text.Format.duration(start, end),
//                    reportFile);
//
//            } catch (IOException ex) {
//                logger.error("Failed to write report {}",
//                    reportFile,
//                    ex);
//            }
//        }
//    }
//
//
//    @SuppressWarnings("UnstableApiUsage")
//    private static class MarkdownReportGenerator extends ReportGenerator {
//
//        private static final char[]                  HexChars          = "0123456789ABCDEF".toCharArray();
//        private static final int                     MAX_BINARY_LENGTH = 40;
//        private static final Map<ResultType, String> RESULT_ICON       = new EnumMap<>(Map.of(
//            ResultType.SUCCESS, Icon.Passed,
//            ResultType.SKIPPED, Icon.Skipped,
//            ResultType.FAILURE, Icon.Failed
//        ));
//
//        private final MarkdownOptions options;
//        private final MarkdownWriter  markdown;
//
//        public MarkdownReportGenerator(Writer writer, MarkdownOptions options) {
//            this.markdown = new MarkdownWriter(writer);
//            this.options  = options;
//        }
//
//        public void generateDetailedReport(String name, TestGraph suite) {
//            var sum = Metrics.sumOf(List.of(suite));
//
//            markdown.h2(Text.italic("Test Results - " + name))
//                .line(Text.bold(sum.total),
//                    " tests were completed in ", Text.bold(Format.duration(sum.duration)),
//                    " with ",
//                    Text.bold(sum.passed), Icon.Passed, "passing, ",
//                    Text.bold(sum.failed), Icon.Failed, "failing, and ",
//                    Text.bold(sum.skipped), Icon.Skipped, "skipped."
//                )
//                .eol();
//
//            suite.traverse(nested -> {
//                for (var test : nested.getTestCases()) {
//                    if (options.isReportable(test.getResult())) {
//                        writeTestCase(test);
//                    }
//                }
//            });
//        }
//
//        private void writeTestCase(TestGraph test) {
//            var descriptor = test.getDescriptor();
//            var result     = test.getResult();
//
//            if (result.getResultType() == ResultType.FAILURE) {
//                markdown.repeat(result.getFailures(), test, this::writeFailure);
//            } else {
//                markdown.text(RESULT_ICON.get(result.getResultType()), " ", descriptor.getDisplayName(), " ")
//                    .sup("<kbd>", Format.duration(result.getEndTime() - result.getStartTime()), "</kbd>")
//                    .line();
//            }
//        }
//
//        private void writeFailure(TestGraph test, TestFailure failure) {
//            var descriptor = test.getDescriptor();
//            var result     = test.getResult();
//            var details    = failure.getDetails();
//
//            // <icon> test name [duration]
//            markdown.text(RESULT_ICON.get(result.getResultType()), " <b>", descriptor.getDisplayName(), "</b>")
//                .sup("<kbd>", Format.duration(result.getEndTime() - result.getStartTime()), "</kbd>")
//                .line()
//                .line("<blockquote>")
//                // <icon> exception name
//                .line(Icon.Warning, " <b>", details.getClassName(), "</b>")
//                .line()
//                // exception message
//                .code(details.getMessage()).eol();
//
//            if (!getFailureType(details).equals("exception")) {
//                // assertion or file comparison
//                markdown.eol()
//                    .line("<b>Expected:</b>")
//                    .raw(Icon.Expected).code(getExpected(details)).eol()
//                    .line()
//                    .line("<b>Actual:</b>")
//                    .raw(Icon.Actual).code(getActual(details)).eol();
//            }
//
//            var permaLink = getPermaLink(descriptor, details);
//            if (!isEmpty(permaLink)) {
//                markdown.line()
//                    .line(getPermaLink(descriptor, details));
//            }
//
//            var stackTrace = details.getStacktrace();
//            if (!isEmpty(stackTrace)) {
//                markdown.line()
//                    .details(Text.italic("Raw Output"),
//                        Text.pre(reduceStackTrace(stackTrace, options::isFilteredStackElement)));
//            }
//
//            markdown.line("</blockquote>")
//                .line();
//        }
//
//        private String getExpected(TestFailureDetails details) {
//            return details.isFileComparisonFailure()
//                   ? data(details.getExpectedContent())
//                   : details.getExpected();
//        }
//
//        private String getActual(TestFailureDetails details) {
//            return details.isFileComparisonFailure()
//                   ? data(details.getActualContent())
//                   : details.getActual();
//        }
//
//        private String data(byte[] data) {
//            if (data == null) {
//                return "null";
//            }
//
//            if (data.length == 0) {
//                return "<empty>";
//            }
//
//            var binary = new StringBuilder();
//            if (isTextContent(data)) {
//                binary.append(new String(data, 0, Math.min(data.length, MAX_BINARY_LENGTH), StandardCharsets.US_ASCII));
//            } else {
//                for (int i = 0; i < MAX_BINARY_LENGTH; i++) {
//                    int b = data[i] & 0xFF;
//                    binary.append(HexChars[b >>> 4]);
//                    binary.append(HexChars[b & 0x0F]);
//                }
//            }
//            if (data.length > MAX_BINARY_LENGTH) {
//                binary.append("...");
//            }
//            return binary.toString();
//        }
//
//        private boolean isTextContent(byte[] data) {
//            int length = Math.min(data.length, MAX_BINARY_LENGTH);
//            for (int i = 0; i < length; i++) {
//                byte b = data[i];
//                if (b == 0) {
//                    return false;
//                }
//                if (b < 32 && b != '\t' && b != '\r' && b != '\n') {
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        private String getPermaLink(TestDescriptor descriptor, TestFailureDetails details) {
//            var sourcePath = options.getSourcePath(descriptor.getClassName());
//            if (isEmpty(sourcePath)) {
//                // Nothing can be done without the source path
//                return "";
//            }
//
//            int line = getFailureLineNumber(descriptor, details, options::resolveSourceFile);
//
//            var blobUrl = options.getGitHubBlobUrl();
//            if (!isEmpty(blobUrl)) {
//                return blobUrl + "/" + sourcePath + getLineSpan(line);
//            }
//
//            return "[/" + sourcePath + "](/" + sourcePath + getLineSpan(line) + ")";
//        }
//
//        private String getLineSpan(int line) {
//            int from = Math.max(0, line - 1);
//            int to   = from + 2;
//            return "#L" + from + "-L" + to;
//        }
//
//        public void generateSummaryReport(String name, List<TestGraph> suites) {
//            var sum = Metrics.sumOf(suites);
//
//            markdown.h2(Text.italic("Test Results - " + name))
//                .line(
//                    Text.bold(sum.total),
//                    " tests were completed in ", Text.bold(Format.duration(sum.duration)),
//                    " with ",
//                    Text.bold(sum.passed), Icon.Passed, "passing, ",
//                    Text.bold(sum.failed), Icon.Failed, "failing, and ",
//                    Text.bold(sum.skipped), Icon.Skipped, "skipped."
//                )
//                .table()
//                .header()
//                .columns("Test suite", "Passed", "Failed", "Skipped", "Duration")
//                .alignments(Align.Left, Align.Right, Align.Right, Align.Center)
//                .end()
//                .rows(suites, (table, suite) -> {
//                    var result = suite.getResult();
//                    table.row(
//                        suite.getDescriptor().getName(),
//                        result.getSuccessfulTestCount() > 0
//                        ? result.getSuccessfulTestCount() + " " + Icon.Passed
//                        : "",
//                        result.getFailedTestCount() > 0
//                        ? result.getFailedTestCount() + " " + Icon.Failed
//                        : "",
//                        result.getSkippedTestCount() > 0
//                        ? result.getSkippedTestCount() + " " + Icon.Skipped
//                        : "",
//                        Format.duration(result.getEndTime() - result.getStartTime())
//                    );
//                })
//                .end();
//        }
//
//        private interface Icon {
//
//            String Skipped  = "&#9898;";
//            String Passed   = "&#9989;";
//            String Failed   = "&#10060;";
//            String Warning  = "&#9888;";
//            String Expected = "&#128313;";
//            String Actual   = "&#128312;";
//        }
//    }
//

    /**
     * Helper class for report generation
     */
    private static class MarkdownOptions extends Options {

        private final String name;

        MarkdownOptions(
            MarkdownReportSettings settings,
            DirectoryProperty outputDir) {

            super(settings, ".md", outputDir.getAsFile().get().toPath());

            this.name = settings.getName();
        }

        public String getName() {
            return name;
        }
    }
}



