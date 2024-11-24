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

import org.gradle.api.Named;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Defines common properties and methods used across different types of report settings. It provides a base
 * implementation which includes a report file and an enabled flag.
 */
public abstract class ReportSettings implements Named {

    private final String name;

    @Inject
    public ReportSettings(String name, Provider<RegularFile> reportFile) {
        this.name = name;
        getEnabled().convention(true);
        getReportFile().convention(reportFile);
    }

    /**
     * Name of the settings type. Used when generating template/class names associated with the settings type.
     */
    @Override
    public @Nonnull String getName() {
        return name;
    }

    /**
     * Whether the report should be enabled.
     *
     * @return A Property object representing the enabled status of the report.
     */
    @Input
    public abstract Property<Boolean> getEnabled();

    /**
     * The name or path of the report file. Should be .md or .json, depending on the report type.
     */
    @Input
    public abstract RegularFileProperty getReportFile();
}
