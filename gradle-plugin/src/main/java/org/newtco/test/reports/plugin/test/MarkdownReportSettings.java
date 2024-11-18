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

import org.gradle.api.Project;
import org.gradle.api.tasks.testing.JUnitXmlReport;

import javax.inject.Inject;

/**
 * Abstract class for configuring settings related to Markdown reports for testing results. It extends
 * {@link ReportSettings} to provide additional configurations specific to Markdown report generation.
 * <p>
 * This class is responsible for configuring settings for Markdown reports, such as including or excluding specific logs
 * or adjusting output settings according to the conventions provided.
 * <p>
 * The configuration settings include whether to include system error logs, system output logs, and whether to output
 * logs per test case. Additionally, it incorporates repository and commit information to generate links to source code
 * files.
 * <p>
 * The class uses Dependency Injection to initialize with required {@code Project} and {@code JUnitXmlReport} instances,
 * which are crucial for determining the conventions and project-specific configurations.
 */
public abstract class MarkdownReportSettings extends ReportSettings {

    @Inject
    public MarkdownReportSettings(String name, Project project, JUnitXmlReport convention) {
        super(name, project, convention);
    }
}
