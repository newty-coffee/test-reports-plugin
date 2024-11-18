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

import org.newtco.test.util.FilterSet;
import org.newtco.test.util.GradleUtils;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.testing.Test;
import org.gradle.util.Configurable;

import javax.inject.Inject;
import java.util.Objects;

public class TestReportsExtension implements Configurable<TestReportsExtension> {

    private final FilterSet              stackFilters;
    private final JsonReportSettings     json;
    private final MarkdownReportSettings summaryMarkdown;
    private final MarkdownReportSettings detailedMarkdown;
    private final DirectoryProperty      outputLocation;
    private final Property<String>       gitLinkRepository;
    private final Property<String>       gitLinkCommit;
    private final Property<String>       gitLinkUrlTemplate;
    private final Project                project;

    @Inject
    public TestReportsExtension(Project project, Test test) {
        var objects = project.getObjects();

        this.gitLinkRepository = objects.property(String.class)
                .convention(GradleUtils.findProperty(project, "GITHUB_REPOSITORY", "CI_PROJECT_PATH_SLUG"));

        this.gitLinkCommit = objects.property(String.class)
                .convention(GradleUtils.findProperty(project, "GITHUB_SHA", "CI_COMMIT_SHA"));

        this.gitLinkUrlTemplate = objects.property(String.class)
                .convention("https://%s/{repository}/blob/{commit}/{file}".formatted(
                        "true".equals(GradleUtils.findProperty(project, "GITLAB_CI"))
                                ? "gitlab.com"
                                : "github.com"));

        this.stackFilters = objects.newInstance(FilterSet.class);
        // By default, include the project group, if set
        var group = Objects.toString(project.getGroup(), null);
        if (group != null) {
            group += group.endsWith(".")
                    ? "**"
                    : ".**";
            stackFilters.include(group);
        }

        var junitXml = test.getReports().getJunitXml();
        json = objects.newInstance(JsonReportSettings.class, project, junitXml);

        summaryMarkdown = objects.newInstance(MarkdownReportSettings.class, "Summary",
                project, junitXml);

        detailedMarkdown = objects.newInstance(MarkdownReportSettings.class, "Detailed",
                project, junitXml);

        outputLocation = objects.directoryProperty().convention(junitXml.getOutputLocation());
        this.project = project;
    }

    /**
     * Retrieves the GitHub repository associated with the report output, used to generate links to source code files.
     * <p>
     * Default value: {@literal GITHUB_REPOSITORY} or {@literal CI_PROJECT_PATH_SLUG} environment variable value.
     *
     * @return a property representing the GitHub repository.
     */
    @Input
    public Property<String> getGitLinkRepository() {
        return gitLinkRepository;
    }

    // For Groovy/Kotlin DSL
    public void setGitLinkRepository(String value) {
        gitLinkRepository.set(value);
    }

    public void setGitLinkRepository(Property<String> value) {
        gitLinkRepository.set(value);
    }


    /**
     * Retrieves the GitHub SHA associated with the report.
     * <p>
     * Default value: {@literal GITHUB_SHA} or {@literal CI_COMMIT_SHA} environment variable value.
     *
     * @return a property representing the GitHub SHA.
     */
    @Input
    public Property<String> getGitLinkCommit() {
        return gitLinkCommit;
    }

    // For Groovy/Kotlin DSL
    public void setGitLinkCommit(String value) {
        gitLinkCommit.set(value);
    }

    public void setGitLinkCommit(Property<String> value) {
        gitLinkCommit.set(value);
    }

    /**
     * Retrieves the URL template used for generating links to the source code files in the report.
     * <p>
     * Supported template replacement parameters:
     * <ul>
     *     <li>repository</li>
     *     <li>commit</li>
     *     <li>file</li>
     * </ul>
     * <p>
     * Default value: {@literal https://github.com/{repository}/blob/{commit}/{file}} or
     * {@literal https://gitlab.com/{repository}/blob/{commit}/{file}}
     *
     * @return a property representing the URL template.
     */
    @Input
    public Property<String> getGitLinkUrlTemplate() {
        return gitLinkUrlTemplate;
    }

    // For Groovy/Kotlin DSL
    public void setGitLinkUrlTemplate(String value) {
        gitLinkUrlTemplate.set(value);
    }

    public void setGitLinkUrlTemplate(Property<String> value) {
        gitLinkUrlTemplate.set(value);
    }

    /**
     * Retrieves the filter set used to include or exclude specific stack elements.
     * <p>
     * Default values: "{project.group}.**"
     * <p>
     *
     * @return the filter set for stack elements.
     */
    @Input
    @Nested
    public FilterSet getStackFilters() {
        return stackFilters;
    }

    /**
     * Configures the stack filters used to include or exclude stack elements based on specified patterns.
     *
     * @param action an action to be executed on the stack filters.
     */
    public void stackFilters(Action<FilterSet> action) {
        action.execute(this.stackFilters);
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

    @Input
    public DirectoryProperty getOutputLocation() {
        return outputLocation;
    }

    /**
     * To work around extensions.configure not using the extension as the delegate
     */
    public TestReportsExtension configure(Action<? super TestReportsExtension> action) {
        action.execute(this);
        return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public TestReportsExtension configure(Closure closure) {
       return configure(
         extension -> {
            closure.setDelegate(extension);
            closure.call(extension);
         }
       );
    }
}
