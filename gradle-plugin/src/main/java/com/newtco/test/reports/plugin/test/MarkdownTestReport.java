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

import com.newtco.test.reports.api.test.TestSettings;
import com.newtco.test.reports.api.test.model.TestSuite;
import com.newtco.test.templates.TemplateInstantiator;
import com.newtco.test.util.Text;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.Test;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class MarkdownTestReport extends TestReport {

    private final MarkdownReportSettings settings;
    private final TemplateInstantiator   instantiator;
    private final MarkdownOptions        options;
    private final Logger                 logger;
    private final String                 projectName;
    private final String                 taskName;

    public MarkdownTestReport(
            TemplateInstantiator instantiator,
            Test test,
            MarkdownReportSettings settings,
            DirectoryProperty outputDir) {
        this.settings     = settings;
        this.instantiator = instantiator;
        this.options      = new MarkdownOptions(settings, outputDir);
        this.logger       = test.getLogger();
        this.projectName  = test.getProject().getName();
        this.taskName     = test.getName();
    }

    private String getTemplateName() {
        // Options name will be either Summary or Detailed
        return "tests." + options.getName() + "MarkdownReportTemplate";
    }

    @Override
    public void generateReport(List<TestSuite> suites) {
        logger.debug("Generating Markdown test report for {}:{}", projectName, taskName);

        if (settings.getAggregateReports().getOrElse(false)) {
            // Merges all suites into a single report
            createAggregatedReports(suites);
        } else {
            // Creates individual reports per test suite
            createIndividualReports(suites);
        }
    }

    private void createAggregatedReports(List<TestSuite> suites) {
        // TEST-(Summary|Detailed).md
        var reportFile = options.resolveReportFile(options.getName().toLowerCase());

        try (var writer = Files.newBufferedWriter(reportFile, StandardOpenOption.CREATE)) {
            var start = Instant.now();
            var template = instantiator.createTemplate(getTemplateName(),
                    writer,
                    projectName + ":" + taskName,
                    accumulatedMetrics(suites),
                    suites,
                    getTemplateSettings());
            template.render();

            var end = Instant.now();

            logger.info("Finished generating aggregated {} markdown results ({}) to: file:///{}",
                    options.getName().toLowerCase(),
                    Text.Format.duration(start, end),
                    reportFile.toString().replace('\\', '/'));

        } catch (Exception ex) {
            throw new GradleException("Failed to write Markdown test report", ex);
        }
    }

    private void createIndividualReports(List<TestSuite> suites) {
        for (var suite : suites) {
            var suiteName  = Objects.requireNonNullElse(suite.getClassName(), suite.getName());
            var reportFile = options.resolveReportFile(options.getName().toLowerCase() + "-" + suiteName);

            try (var writer = Files.newBufferedWriter(reportFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                var start = Instant.now();
                var template = instantiator.createTemplate(getTemplateName(),
                        writer,
                        projectName + ":" + taskName + " - " + suite.getName(),
                        suite,
                        List.of(suite),
                        getTemplateSettings());
                template.render();
                var end = Instant.now();

                logger.info("Finished generating individual {} markdown results ({}) to: file:///{}",
                        options.name.toLowerCase(),
                        Text.Format.duration(start, end),
                        reportFile.toString().replace('\\', '/'));

            } catch (Exception ex) {
                throw new GradleException("Failed to write Markdown test report", ex);
            }
        }
    }

    /**
     * Generates the settings class used within a template
     */
    private TestSettings getTemplateSettings() {
        var markdown = new TestSettings();
        markdown.aggregateReport     = settings.getAggregateReports().get();
        markdown.includeSystemErrLog = settings.getIncludeSystemErrLog().get();
        markdown.includeSystemOutLog = settings.getIncludeSystemOutLog().get();
        markdown.outputPerTestCase   = settings.getOutputPerTestCase().get();
        markdown.statuses            = Set.copyOf(settings.getTestOutcomes().get());
        return markdown;
    }

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



