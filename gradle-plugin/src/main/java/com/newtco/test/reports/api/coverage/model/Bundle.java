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

package com.newtco.test.reports.api.coverage.model;

import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;


/**
 * The Bundle class represents coverage data for a bundle of classes or packages. Extends the Coverage class with the
 * IBundleCoverage type parameter.
 */
public class Bundle extends Coverage<IBundleCoverage> {

    public Bundle(IBundleCoverage bundleCoverage) {
        super(bundleCoverage);
    }

    public String getName() {
        return coverage.getName();
    }

    public List<Package> getPackages() {
        return coverage.getPackages().stream()
            .map(Package::new)
            .sorted(Comparator.comparing(Package::getName))
            .toList();
    }
}
