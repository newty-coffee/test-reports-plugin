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

import com.newtco.test.reports.api.test.model.Status;
import com.newtco.test.reports.plugin.PluginVersion;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.testing.JUnitXmlReport;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public abstract class ReportSettings implements Named {

    protected final String name;

    @Inject
    public ReportSettings(String name, Project project, JUnitXmlReport jUnitXmlReport) {
        this.name = name;

        getEnabled().convention(true);
        getAggregateReports().convention(false);
        if (PluginVersion.isGradleVersionAtLeast("8.8")) {
            getIncludeSystemErrLog().convention(jUnitXmlReport.getIncludeSystemErrLog());
            getIncludeSystemOutLog().convention(jUnitXmlReport.getIncludeSystemOutLog());
        } else {
            getIncludeSystemErrLog().convention(true);
            getIncludeSystemOutLog().convention(true);
        }
        getOutputPerTestCase().convention(jUnitXmlReport.isOutputPerTestCase());
        getTestOutcomes().convention(Set.of(Status.FAILED));
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    /**
     * Whether the report generation is enabled.
     */
    @Input
    public abstract Property<Boolean> getEnabled();

    /**
     * Determines if the reports should be aggregated.
     *
     * @return a Property<Boolean> that represents whether the reports will be aggregated
     */
    @Input
    public abstract Property<Boolean> getAggregateReports();

    /**
     * Whether the system error log should be included in the report.
     */
    @Input
    public abstract Property<Boolean> getIncludeSystemErrLog();

    /**
     * Whether the system out log should be included in the report.
     */
    @Input
    public abstract Property<Boolean> getIncludeSystemOutLog();

    /**
     * Whether to output system logs per test case or roll up to the test suite.
     */
    @Input
    public abstract Property<Boolean> getOutputPerTestCase();

    /**
     * Set of test outcomes to be included in the report.
     */
    @Input
    public abstract SetProperty<Status> getTestOutcomes();

    /**
     * Sets the test outcomes be included in the report.
     *
     * @param outcomes Varargs parameter representing the test outcomes. This can be instances of
     *                 {@code TestResult.ResultType}, {@code Outcome} or strings of either type.
     */
    public void testOutcomes(Object... outcomes) {
        for (var outcome : outcomes) {
            var status = switch (String.valueOf(outcome).toUpperCase()) {
                case "PASSED", "SUCCESS" -> Status.PASSED;
                case "FAILED", "FAILURE" -> Status.FAILED;
                case "SKIPPED" -> Status.SKIPPED;
                default -> null;
            };
            if (status != null) {
                getTestOutcomes().add(status);
            } else {
                throw new IllegalArgumentException("Invalid test outcome: " + outcome
                        + ". Must be one of PASSED, SUCCESS, FAILED, FAILURE or SKIPPED");
            }
        }
    }
}
