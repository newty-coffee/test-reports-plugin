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

import com.newtco.test.reports.api.test.model.*;
import com.newtco.test.util.JsonWriter;
import com.newtco.test.util.Text.Format;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * JsonTestReport generates test reports in JSON format based on the results of test execution. It extends the abstract
 * TestReport class and provides methods to generate both aggregated and individual test reports.
 */
public class JsonTestReport extends TestReport {

    private final JsonOptions options;
    private final Logger      logger;

    public JsonTestReport(Test test, JsonReportSettings settings, DirectoryProperty outputDir) {
        this.options = new JsonOptions(settings, outputDir);
        this.logger  = test.getLogger();
    }

    @Override
    public void generateReport(List<TestSuite> suites) {
        if (options.aggregated) {
            createAggregatedReports(suites);
        } else {
            createIndividualReports(suites);
        }
    }

    private void createAggregatedReports(List<TestSuite> suites) {
        var reportFile = options.resolveReportFile("");
        try (var writer = Files.newBufferedWriter(reportFile)) {
            var start = Instant.now();
            new JsonReportGenerator(writer, options).generate(suites);
            var end = Instant.now();

            logger.info("Finished generating aggregated JSON results ({}) to: file:///{}",
                    Format.duration(start, end),
                    reportFile.toString().replace('\\', '/'));

        } catch (IOException ex) {
            logger.error("Failed to write report {}",
                    reportFile,
                    ex);
        }
    }

    private void createIndividualReports(List<TestSuite> suites) {
        for (var suite : suites) {
            var suiteName  = Objects.requireNonNullElse(suite.getClassName(), suite.getName());
            var reportFile = options.resolveReportFile(suiteName);

            try (var writer = Files.newBufferedWriter(reportFile)) {
                var start = Instant.now();
                new JsonReportGenerator(writer, options).generate(List.of(suite));
                var end = Instant.now();

                logger.info("Finished generating individual JSON results ({}) to: file:///{}",
                        Format.duration(start, end),
                        reportFile.toString().replace('\\', '/'));

            } catch (IOException ex) {
                logger.error("Failed to write report {}",
                        suiteName,
                        ex);
            }
        }
    }

    /**
     * JsonReportGenerator is responsible for generating test reports in JSON format. It extends the abstract
     * ReportGenerator class.
     */
    private static class JsonReportGenerator {

        private final JsonOptions options;
        private final JsonWriter  json;

        public JsonReportGenerator(Writer writer, JsonOptions options) {
            this.json    = new JsonWriter(writer);
            this.options = options;
        }

        public void generate(List<TestSuite> suites) {
            var metrics = accumulatedMetrics(suites);

            json.raw('{')
                    .field("tests").value(metrics.total).comma()
                    .field("skipped").value(metrics.skipped).comma()
                    .field("failures").value(metrics.failed).comma()
                    .field("timestamp").value(timestamp(metrics.startTime)).comma()
                    .field("time").raw(seconds(metrics.duration)).comma()
                    .field("testSuites").array(suites, this::writeTestSuite);
            json.raw('}');
        }

        private void writeTestSuite(TestSuite suite) {
            json.raw('{')
                    .field("name").value(suite.getName()).comma()
                    .field("displayName").value(suite.getDisplayName()).comma()
                    .field("className").value(suite.getClassName()).comma()
                    .field("tests").value(suite.getTotal()).comma()
                    .field("skipped").value(suite.getSkipped()).comma()
                    .field("failures").value(suite.getFailed()).comma()
                    .field("timestamp").value(timestamp(suite.getStartTime())).comma()
                    .field("time").raw(seconds(suite.getDuration())).comma()
                    .field("hostname").value(HOSTNAME).comma()
                    .field("stderr").value(suite.getStdErr()).comma()
                    .field("stdout").value(suite.getStdOut()).comma()
                    .field("testCases").array(suite.getTests().stream()
                            .filter(this::isReportable).toList(), this::writeTestCase);
            json.raw('}');
        }

        private String timestamp(long result) {
            return new Date(result)
                    .toInstant()
                    .atOffset(ZoneOffset.of("Z"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        private String seconds(long duration) {
            long seconds = duration / 1000;
            long millis  = duration % 1000;

            var output = new StringBuilder(19 + 4);
            output.append(seconds).append('.');
            if (millis < 10) {
                output.append("00");
            } else if (millis < 100) {
                output.append("0");
            }
            output.append(millis);

            return output.toString();
        }

        private boolean isReportable(TestCase test) {
            return options.isReportable(test.getStatus());
        }

        private void writeTestCase(TestCase test) {
            json.raw('{')
                    .field("status").value(test.getStatus().name().toLowerCase()).comma()
                    .field("name").value(test.getName()).comma()
                    .field("displayName").value(test.getDisplayName()).comma()
                    .field("className").value(test.getClassName()).comma()
                    .field("file").value(test.getSourceFile()).comma()
                    .field("url").value(test.getUrl()).comma()
                    .field("time").raw(seconds(test.getDuration())).comma()
                    .field("stderr").value(test.getStdErr()).comma()
                    .field("stdout").value(test.getStdOut()).comma()
                    .field("failures").array(test.getFailures(), test, this::writeFailure);
            json.raw('}');
        }

        private void writeFailure(TestCase test, Failure failure) {
            json.raw('{')
                    .field("message").value(failure.getMessage()).comma()
                    .field("className").value(test.getClassName()).comma()
                    .field("line").value(failure.getLineNumber()).comma()
                    .field("stackTrace").value(failure.getStackTrace()).comma()
                    .field("type").value(failure.getType().name().toLowerCase()).comma()
                    .field("expected").value(getExpected(failure)).comma()
                    .field("actual").value(getActual(failure)).comma()
                    .field("causes").array(failure.getCauses(), test, this::writeFailure);
            json.raw('}');
        }

        private String getExpected(Failure failure) {
            if (failure instanceof FileFailure file) {
                return data(file.getExpectedData());
            } else if (failure instanceof AssertionFailure assertion) {
                return assertion.getExpected();
            }

            return null;
        }

        private String getActual(Failure failure) {
            if (failure instanceof FileFailure file) {
                return data(file.getActualData());
            } else if (failure instanceof AssertionFailure assertion) {
                return assertion.getActual();
            }

            return null;
        }


        private String data(byte[] content) {
            if (content != null) {
                return "data:" + Base64.getEncoder().encodeToString(content);
            }
            return null;
        }
    }

    static class JsonOptions extends Options {

        boolean aggregated;

        JsonOptions(
                JsonReportSettings settings,
                DirectoryProperty outputDir) {
            super(settings, ".json", outputDir.getAsFile().get().toPath());

            this.aggregated = settings.getAggregateJsonReports().get();
        }
    }

}




