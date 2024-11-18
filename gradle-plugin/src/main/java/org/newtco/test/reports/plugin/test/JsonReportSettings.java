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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.testing.JUnitXmlReport;

import javax.inject.Inject;

/**
 * Abstract class that extends {@link ReportSettings} to provide specific settings for JSON reports.
 */
public abstract class JsonReportSettings extends ReportSettings {


    @Inject
    public JsonReportSettings(Project project, JUnitXmlReport convention) {
        super("Json", project, convention);

        getAggregateJsonReports().convention(true);
    }

    /**
     * Whether the JSON reports should be aggregated across all tests into a single report.
     *
     * @return a property that holds a boolean value indicating if the JSON reports should be aggregated
     */
    @Input
    public abstract Property<Boolean> getAggregateJsonReports();
}
