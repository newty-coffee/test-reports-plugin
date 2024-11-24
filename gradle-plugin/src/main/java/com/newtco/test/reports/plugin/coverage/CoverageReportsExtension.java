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

package com.newtco.test.reports.plugin.coverage;

import com.newtco.test.util.Gradle;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.gradle.util.Configurable;

import javax.inject.Inject;

/**
 * Extension class for this plugin to hold configurable properties for additional Jacoco reports
 */
public class CoverageReportsExtension implements Configurable<CoverageReportsExtension> {

    private final Property<String>       gitBaseRef;
    private final PatternSet             changeSet;
    private final JsonReportSettings     json;
    private final MarkdownReportSettings summaryMarkdown;
    private final MarkdownReportSettings detailedMarkdown;

    @Inject
    public CoverageReportsExtension(Project project, JacocoReport report) {
        var objects = project.getObjects();

        this.gitBaseRef = objects.property(String.class).convention(Gradle.findProperty(project,
                "GITHUB_BASE_REF", "CI_MERGE_REQUEST_TARGET_BRANCH_NAME"));
        this.changeSet  = new PatternSet();

        // Use the xml report's file location to determine where to store ours
        var reportsDir = objects.directoryProperty().fileValue(
                report.getReports().getXml().getOutputLocation().getAsFile().get().getParentFile()
        );

        json = objects.newInstance(JsonReportSettings.class,
                reportsDir.file(report.getName() + ".json"));

        summaryMarkdown  = objects.newInstance(MarkdownReportSettings.class,
                "Summary",
                reportsDir.file(report.getName() + "Summary.md"));
        detailedMarkdown = objects.newInstance(MarkdownReportSettings.class,
                "Detailed",
                reportsDir.file(report.getName() + ".md"));
    }

    /**
     * Retrieves the base Git reference.
     *
     * @return A Property object containing the Git base reference as a String.
     */
    @Input
    @Optional
    public Property<String> getGitBaseRef() {
        return this.gitBaseRef;
    }

    /**
     * The list of Java source files to include or exclude in the coverage report. The default root of the file tree is
     * the root project dir, making it compatible with git repo root relative paths.
     */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public PatternSet getChangeSet() {
        return this.changeSet;
    }

    /**
     * For closure configuration syntax
     */
    public void changeSet(Action<? super PatternSet> action) {
        action.execute(getChangeSet());
    }

    /**
     * Configuration for the JSON report
     */
    @Input
    @Nested
    public JsonReportSettings getJson() {
        return json;
    }

    /**
     * For closure configuration support
     */
    public void json(Action<? super JsonReportSettings> action) {
        action.execute(getJson());
    }


    @Input
    @Nested
    public MarkdownReportSettings getSummaryMarkdown() {
        return summaryMarkdown;
    }

    /**
     * For closure configuration support
     */
    public void summaryMarkdown(Action<? super MarkdownReportSettings> action) {
        action.execute(getSummaryMarkdown());
    }

    /**
     * Configuration for the Markdown report
     */
    @Input
    @Nested
    public MarkdownReportSettings getDetailedMarkdown() {
        return detailedMarkdown;
    }

    /**
     * For closure configuration support
     */
    public void detailedMarkdown(Action<? super MarkdownReportSettings> action) {
        action.execute(getDetailedMarkdown());
    }

    /**
     * To work around extensions.configure not using the extension as the delegate
     */
    public CoverageReportsExtension configure(Action<? super CoverageReportsExtension> action) {
        action.execute(this);
        return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public CoverageReportsExtension configure(Closure closure) {
        return configure(
                extension -> {
                    closure.setDelegate(extension);
                    closure.call(extension);
                }
        );
    }
}



