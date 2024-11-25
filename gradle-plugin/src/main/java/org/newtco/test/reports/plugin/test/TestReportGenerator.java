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

package org.newtco.test.reports.plugin.test;

import org.newtco.test.reports.api.test.model.TestSuite;
import org.newtco.test.templates.TemplateInstantiator;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;

import java.util.List;

import static org.newtco.test.util.GradleUtils.Extensions.extensionOf;

/**
 * The {@code TestReportGenerator} class is responsible for generating test reports in JSON and Markdown formats based
 * on the results of test executions.
 * <p>
 * It works with the TestReportsExtension which provides configurations for generating the reports, and utilizes the
 * TestGraphCollector to gather test data.
 */
public class TestReportGenerator {

    private final Project              project;
    private final Test                 test;
    private final TestReportsExtension extension;
    private final TestSuiteCollector   collector;
    private final TemplateInstantiator instantiator;


    public TestReportGenerator(TemplateInstantiator templateInstantiator, Test testTask, TestSuiteCollector collector) {
        this.project      = testTask.getProject();
        this.test         = testTask;
        this.extension    = extensionOf(testTask, TestReportsExtension.class);
        this.collector    = collector;
        this.instantiator = templateInstantiator;
    }

    public void generateTestReports() {
        deleteReports();

        var suites = collector.getTestSuites(test);
        generateMarkdownReports(suites);
        generateJsonReports(suites);
    }

    /**
     * Deletes .json and .md reports from the reports directory
     */
    private void deleteReports() {
        var reportsDir = extension.getOutputLocation().getAsFile().getOrNull();
        if (null != reportsDir && reportsDir.exists()) {
            Object[] reports = reportsDir.listFiles((dir, name) -> name.startsWith("TEST-") &&
                    (name.endsWith(".json") || name.endsWith(".md")));
            if (reports != null && reports.length > 0) {
                project.delete(reports);
            }
        }
    }

    private void generateJsonReports(List<TestSuite> suites) {
        var settings = extension.getJson();
        if (Boolean.TRUE.equals(settings.getEnabled().get())) {
            new JsonTestReport(test, settings, extension.getOutputLocation())
                    .generateReport(suites);
        }
    }

    private void generateMarkdownReports(List<TestSuite> suites) {
        var settings = extension.getSummaryMarkdown();
        if (Boolean.TRUE.equals(settings.getEnabled().get())) {
            new MarkdownTestReport(instantiator, test, settings, extension.getOutputLocation())
                    .generateReport(suites);
        }

        settings = extension.getDetailedMarkdown();
        if (Boolean.TRUE.equals(settings.getEnabled().get())) {
            new MarkdownTestReport(instantiator, test, settings, extension.getOutputLocation())
                    .generateReport(suites);
        }
    }
}



