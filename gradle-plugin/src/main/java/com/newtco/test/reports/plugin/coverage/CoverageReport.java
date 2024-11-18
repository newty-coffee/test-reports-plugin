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

import org.gradle.api.Project;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jacoco.core.analysis.IBundleCoverage;

/**
 * The CoverageReport interface defines the contract for generating coverage reports for a given project using JaCoCo
 * and bundle coverage data. Implementations of this interface can generate different types of coverage reports like
 * Markdown, JSON, etc.
 */
interface CoverageReport {
    void generateReport(Project project, JacocoReport report, IBundleCoverage bundle);
}



