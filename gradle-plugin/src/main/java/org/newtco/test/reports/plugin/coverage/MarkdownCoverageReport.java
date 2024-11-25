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

import org.newtco.test.reports.api.coverage.Badge;
import org.newtco.test.reports.api.coverage.CoverageSettings;
import org.newtco.test.reports.api.coverage.model.Bundle;
import org.newtco.test.templates.TemplateInstantiator;
import org.newtco.test.util.Text;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jacoco.core.analysis.IBundleCoverage;

import java.nio.file.Files;
import java.time.Instant;

/**
 * The MarkdownCoverageReport class generates a Markdown-based code coverage report using JaCoCo and bundle coverage
 * data. It implements the CoverageReport interface, allowing it to generate reports for given projects.
 * <p>
 * Markdown coverage reports are template based, relying on the embedded coverage templates, or replacements that users
 * may provide.
 */
public class MarkdownCoverageReport implements CoverageReport {

    private final MarkdownReportSettings settings;
    private final TemplateInstantiator   instantiator;

    public MarkdownCoverageReport(TemplateInstantiator instantiator, MarkdownReportSettings settings) {
        this.instantiator = instantiator;
        this.settings     = settings;
    }

    private String getTemplateName() {
        // Settings name will be either Summary or Detailed
        return "coverage." + settings.getName() + "MarkdownReportTemplate";
    }


    @Override
    public void generateReport(Project project, JacocoReport report, IBundleCoverage bundle) {
        var log = report.getLogger();

        log.debug("Generating Markdown coverage report for {}:{}", project.getName(), report.getName());

        var outputFile = settings.getReportFile().getAsFile().get().toPath();

        try (var writer = Files.newBufferedWriter(outputFile)) {
            var start = Instant.now();
            var template = instantiator.createTemplate(getTemplateName(),
                    writer,
                    getTemplateSettings(),
                    new Bundle(bundle),
                    new Badge(settings.getBadgeStyle().getOrElse("flat"),
                            settings.getCurrentColorScheme().getColors())
            );
            template.render();
            var end = Instant.now();

            log.lifecycle("Finished generating Markdown coverage report ({}) to: file:///{}",
                    Text.Format.duration(start, end),
                    outputFile.toString().replace('\\', '/'));
        } catch (Exception e) {
            throw new GradleException("Failed to write Markdown coverage report", e);
        }
    }

    /**
     * Transforms from extension settings to API settings.Settings supported are the same, just without the Gradle
     * verbosity of providers and such so accessing them from templates is cleaner.
     */
    private CoverageSettings getTemplateSettings() {
        var markdown = new CoverageSettings();
        markdown.abbreviatePackages = settings.getAbbreviatePackages().get();
        return markdown;
    }
}


