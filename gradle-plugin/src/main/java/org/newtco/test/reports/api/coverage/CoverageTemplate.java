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

package org.newtco.test.reports.api.coverage;

import org.newtco.test.reports.api.MarkdownTemplate;
import org.newtco.test.reports.api.coverage.model.Bundle;
import org.newtco.test.reports.api.coverage.model.Coverage;

import java.io.Writer;

/**
 * Base class for coverage report templates, providing the core contextual properties/methods available to the render()
 * method
 */
public abstract class CoverageTemplate<T extends CoverageTemplate<T>> extends MarkdownTemplate<T> {
    protected final CoverageSettings settings;
    protected final Bundle           bundle;
    protected final Badge            badge;

    public CoverageTemplate(Writer writer, CoverageSettings settings, Bundle bundle, Badge badge) {
        super(writer);
        this.settings = settings;
        this.bundle   = bundle;
        this.badge    = badge;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Badge getBadgeGenerator() {
        return badge;
    }

    /**
     * Generates a coverage badge for the given coverage counter.
     *
     * @param counter the counter containing the coverage statistics
     * @return a string representing the generated badge with coverage statistics
     */
    public String badge(Coverage.Counter counter) {
        return badge.create(counter);
    }
}
