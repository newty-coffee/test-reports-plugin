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

import org.newtco.test.templates.TemplateInstantiator;
import org.newtco.test.util.GitExecutor;
import org.newtco.test.util.JdepsDependencyCollector;
import org.newtco.test.util.SourceSetCollectors;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.newtco.test.util.GradleUtils.Extensions.extensionOf;

/**
 * Responsible for generating coverage reports for a given project. It supports generating JSON and Markdown formatted
 * reports based on the coverage data collected by Jacoco.
 */
public class CoverageReportGenerator {

    private final JacocoReport            jacocoReport;
    private final Project                 project;
    private final Logger                   log;
    private final CoverageReportsExtension extension;
    private final TemplateInstantiator     instantiator;

    public CoverageReportGenerator(TemplateInstantiator templateInstantiator, JacocoReport jacocoReport) {
        this.jacocoReport = jacocoReport;
        this.extension    = extensionOf(jacocoReport, CoverageReportsExtension.class);
        this.project      = jacocoReport.getProject();
        this.log          = jacocoReport.getLogger();
        this.instantiator = templateInstantiator;
    }

    public void generateCoverageReports() {
        if (!Boolean.TRUE.equals(extension.getJson().getEnabled().get())
                && !Boolean.TRUE.equals(extension.getSummaryMarkdown().getEnabled().get())
                && !Boolean.TRUE.equals(extension.getDetailedMarkdown().getEnabled().get())) {
            log.lifecycle(
                    "Test Reports Plugin: Skipping JSON/Markdown coverage report generation. Neither report is enabled.");
            return;
        }

        deleteReports();

        // Analyze the files in the execution data created by the jacoco instrumented run
        var executionDataFiles = jacocoReport.getExecutionData().getFiles();
        if (executionDataFiles.isEmpty()) {
            log.lifecycle("Test Reports Plugin: Skipping coverage report generation. Execution data is empty.");
            return;
        }

        // Determine the classes we want to report on
        var classFiles = getCoverageTargets();
        if (classFiles.isEmpty()) {
            log.lifecycle("Test Reports Plugin: Skipping coverage report generation. No classes found to analyze.");
            return;
        }

        try {
            var execFileLoader = new ExecFileLoader();
            for (var file : executionDataFiles) {
                execFileLoader.load(file);
            }

            var coverage = new CoverageBuilder();
            var analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverage);

            log.lifecycle("Analyzing {} class files for JSON/Markdown coverage reports", classFiles.size());

            for (var classFile : classFiles) {
                analyzer.analyzeAll(classFile);
            }

            var bundle = coverage.getBundle(project.getName());
            generateMarkdownReports(bundle);
            generateJsonReport(bundle);


        } catch (IOException e) {
            throw new GradleException("Failed to generate coverage reports: %s".formatted(e.getMessage()), e);
        }
    }

    private void deleteReports() {
        project.delete(
                extension.getJson().getReportFile().get().getAsFile(),
                extension.getSummaryMarkdown().getReportFile().get().getAsFile(),
                extension.getDetailedMarkdown().getReportFile().get().getAsFile());
    }

    private Set<File> getCoverageTargets() {
        var baseRef = extension.getGitBaseRef().getOrNull();
        if (null != baseRef && !baseRef.isBlank()) {
            var projectDirName = project.getRootDir().toPath().relativize(project.getProjectDir().toPath()).toString();
            if (projectDirName.isBlank()) {
                projectDirName = ".";
            }

            GitExecutor.execGit(project,
                    List.of(
                            "--no-pager",
                            "diff",
                            "--name-only",
                            baseRef,
                            "HEAD",
                            "--",
                            "%s/**/*.java".formatted(projectDirName)
                    ),
                    result -> {
                        if (result.getExitValue() == 0) {
                            var files = result.getOutput().lines().toList();
                            if (!files.isEmpty()) {
                                extension.getChangeSet().include(files);
                            }
                        } else {
                            log.error("Error executing '{}'\nExit value: {}\nError: {}",
                                    result.getCommand(),
                                    result.getExitValue(),
                                    result.getError());
                        }
                    });
        }

        Spec<FileTreeElement> changeSetSpec = extension.getChangeSet().isEmpty()
                ? (unused) -> true
                : extension.getChangeSet().getAsSpec();

        return getReportableClasses(changeSetSpec);
    }


    public Set<File> getReportableClasses(Spec<FileTreeElement> changeSet) {
        var sourceSets = extensionOf(project, JavaPluginExtension.class)
                .getSourceSets().stream()
                .collect(Collectors.partitioningBy(SourceSet::isMain));

        SourceSetCollectors.SourceFileTreeFilter filter = (unused, fileTreeElement) -> changeSet.isSatisfiedBy(
                fileTreeElement);

        // If a unit/integration test is updated, we want to include classes under test without them
        // having to be modified and included in any change set
        var dependencies = new JdepsDependencyCollector().getDependencies(
                sourceSets.get(false).stream().collect(SourceSetCollectors.classFilesMatching(filter)),
                JdepsDependencyCollector.outerClassNameMapper(project.getGroup() + "."));
        if (!dependencies.isEmpty()) {
            // Also include test dependencies when filtering the main source set
            filter = filter.or((qualifiedName, unused) -> dependencies.contains(qualifiedName));
        }

        return sourceSets.get(true).stream()
                .collect(SourceSetCollectors.classFilesMatching(filter));
    }


    private void generateMarkdownReports(IBundleCoverage bundle) {
        var settings = extension.getSummaryMarkdown();
        if (Boolean.TRUE.equals(settings.getEnabled().get())) {
            new MarkdownCoverageReport(instantiator, settings).generateReport(project, jacocoReport, bundle);
        }

        settings = extension.getDetailedMarkdown();
        if (Boolean.TRUE.equals(settings.getEnabled().get())) {
            new MarkdownCoverageReport(instantiator, settings).generateReport(project, jacocoReport, bundle);
        }
    }

    private void generateJsonReport(IBundleCoverage bundle) {
        var settings = extension.getJson();
        if (Boolean.TRUE.equals(settings.getEnabled().get())) {
            new JsonCoverageReport(settings).generateReport(project, jacocoReport, bundle);
        }
    }
}



