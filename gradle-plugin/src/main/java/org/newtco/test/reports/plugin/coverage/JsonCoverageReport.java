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

package org.newtco.test.reports.plugin.coverage;

import org.newtco.test.reports.api.coverage.model.Names;
import org.newtco.test.util.JsonWriter;
import org.newtco.test.util.Text;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jacoco.core.analysis.*;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

/**
 * Class responsible for generating JSON coverage reports. Implements the CoverageReport interface to adhere to a
 * standard method of report generation.
 *
 * @implNote Does not support templating like the markdown reports
 */
public class JsonCoverageReport implements CoverageReport {
    private final JsonReportSettings settings;

    public JsonCoverageReport(JsonReportSettings settings) {
        this.settings = settings;
    }

    @Override
    public void generateReport(Project project, JacocoReport report, IBundleCoverage bundle) {
        generateReport(project, bundle);
    }

    private void generateReport(Project project, IBundleCoverage bundle) {
        var reportFile = settings.getReportFile().getAsFile().get().toPath();

        try (var writer = Files.newBufferedWriter(reportFile)) {
            var start = Instant.now();
            new JsonReportGenerator(writer, settings).generate(bundle);
            var end = Instant.now();

            project.getLogger().lifecycle("Finished generating JSON coverage report ({}) to: file:///{}",
                    Text.Format.duration(start, end),
                    reportFile.toString().replace('\\', '/'));
        } catch (IOException e) {
            throw new GradleException("Failed to write JSON coverage report", e);
        }
    }


    private static class JsonReportGenerator {
        private final boolean    includeLines;
        private final boolean    includeClasses;
        private final boolean    includeMethods;
        private final boolean    includeSources;
        private final boolean    simpleCounters;
        private final JsonWriter json;

        public JsonReportGenerator(Writer writer, JsonReportSettings settings) {
            this.includeLines   = settings.getIncludeLines().get();
            this.includeClasses = settings.getIncludeClasses().get();
            this.includeMethods = settings.getIncludeMethods().get();
            this.includeSources = settings.getIncludeSources().get();
            this.simpleCounters = settings.getSimplifiedCounters().get();
            this.json           = new JsonWriter(writer);
        }

        public void generate(IBundleCoverage bundle) {
            json.raw('{')
                    .field("report").value(bundle, this::writeReport);
            json.raw('}');

        }

        private void writeReport(IBundleCoverage bundle) {
            json.raw('{')
                    .field("name").value(bundle.getName()).comma()
                    .field("counters").value(bundle, this::writeCounters).comma()
                    .field("packages").array(bundle.getPackages(), bundle, this::writePackage);
            json.raw('}');
        }

        private void writePackage(IBundleCoverage bundle, IPackageCoverage pkg) {
            json.raw('{')
                    .field("name").value(Names.getPackageName(pkg.getName())).comma()
                    .field("counters").value(pkg, this::writeCounters).comma()
                    .field("classes").array(
                            includeClasses ? pkg.getClasses() : List.of(),
                            pkg,
                            this::writeClass).comma()
                    .field("sourceFiles").array(
                            includeSources ? pkg.getSourceFiles() : List.of(),
                            pkg,
                            this::writeSourceFile);
            json.raw('}');
        }


        private void writeClass(IPackageCoverage pkg, IClassCoverage classFile) {
            json.raw('{')
                    .field("name").value(Names.getClassName(classFile)).comma()
                    .field("sourceFile").value(classFile.getSourceFileName()).comma()
                    .field("counters").value(classFile, this::writeCounters).comma()
                    .field("methods").array(
                            includeMethods ? classFile.getMethods() : List.of(),
                            classFile,
                            this::writeMethod);
            json.raw('}');
        }

        private void writeMethod(IClassCoverage clazz, IMethodCoverage method) {
            json.raw('{')
                    .field("name").value(method.getName()).comma()
                    .field("signature").value(Names.getMethodName(clazz, method)).comma()
                    .field("line").raw('{')
                    .field("first").value(method.getFirstLine()).comma()
                    .field("last").value(method.getLastLine())
                    .raw('}').comma()
                    .field("counters").value(method, this::writeCounters);
            json.raw('}');
        }

        private void writeSourceFile(IPackageCoverage pkg, ISourceFileCoverage sourceFile) {
            json.raw('{')
                    .field("name").value(sourceFile.getName()).comma()
                    .field("counters").value(sourceFile, this::writeCounters).comma()
                    .field("lines").array(
                            includeLines ? new LineIterator(sourceFile) : new LineIterator(),
                            sourceFile,
                            this::writeLine);
            json.raw('}');
        }

        private void writeLine(ISourceFileCoverage sourceFile, int lineNumber) {
            var line = sourceFile.getLine(lineNumber);

            if (line.getStatus() != ICounter.EMPTY) {
                json.raw('{')
                        .field("number").value(lineNumber).comma()
                        .field("instructions").value(line.getInstructionCounter(), this::writeCounter).comma()
                        .field("branches").value(line.getBranchCounter(), this::writeCounter).comma();
                json.raw('}');
            }
        }

        private void writeCounters(ICoverageNode coverage) {
            json.raw('{')
                    .field("instruction").value(coverage.getInstructionCounter(), this::writeCounter).comma()
                    .field("branch").value(coverage.getBranchCounter(), this::writeCounter).comma()
                    .field("line").value(coverage.getLineCounter(), this::writeCounter).comma()
                    .field("complexity").value(coverage.getComplexityCounter(), this::writeCounter).comma()
                    .field("class").value(coverage.getClassCounter(), this::writeCounter).comma()
                    .field("method").value(coverage.getMethodCounter(), this::writeCounter);
            json.raw('}');
        }

        private void writeCounter(ICounter counter) {
            if (simpleCounters) {
                var total = counter.getCoveredCount() + counter.getMissedCount();
                if (total == 0) {
                    json.value(0);
                } else {
                    json.value(BigDecimal.valueOf(100.0f * ((double) counter.getCoveredCount() / total))
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue());
                }
            } else {
                json.raw('{')
                        .field("covered").value(counter.getCoveredCount()).comma()
                        .field("missed").value(counter.getMissedCount())
                        .raw('}');
            }
        }

        private static class LineIterator implements Iterator<Integer> {
            private final int last;
            private       int current;

            // Constructs an empty line iterator, used when line output is disabled
            public LineIterator() {
                this.last    = 0;
                this.current = 0;
            }

            public LineIterator(ISourceFileCoverage sourceFile) {
                this.last    = sourceFile.getLastLine();
                this.current = sourceFile.getFirstLine();
            }

            @Override
            public boolean hasNext() {
                return current <= last;
            }

            @Override
            public Integer next() {
                return current++;
            }
        }
    }
}


