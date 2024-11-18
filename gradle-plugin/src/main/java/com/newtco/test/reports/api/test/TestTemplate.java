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

package com.newtco.test.reports.api.test;

import java.io.Writer;
import java.util.List;

import com.newtco.test.reports.api.MarkdownTemplate;
import com.newtco.test.reports.api.test.model.Stats;
import com.newtco.test.reports.api.test.model.TestSuite;

public abstract class TestTemplate<T extends TestTemplate<T>> extends MarkdownTemplate<T> {
    protected final TestSettings    settings;
    protected final String          title;
    protected final List<TestSuite> suites;
    protected final Stats           stats;


    public TestTemplate(Writer writer, String title, Stats stats, List<TestSuite> suites, TestSettings settings) {
        super(writer);

        this.settings = settings;
        this.stats    = stats;
        this.title    = title;
        this.suites   = suites;
    }

    protected String getTitle() {
        return title;
    }

    protected List<TestSuite> getTestSuites() {
        return suites;
    }

    protected TestSuite getTestSuite() {
        if (suites.size() != 1) {
            throw new IllegalStateException("Expected 1 test suites, found " + suites.size());
        }

        return suites.get(0);
    }

    protected TestSettings getSettings() {
        return settings;
    }

    protected Stats getStats() {
        return stats;
    }

    protected interface Icons {
        // Test outcomes
        String Passed  = "&#9989;";
        String Skipped = "&#9898;";
        String Failed  = "&#10060;";

        // Miscellaneous
        String Warning  = "&#9888;";
        String Expected = "&#128313;";
        String Actual   = "&#128312;";

        static String passed(Stats stats) {
            return stats.passed > 0 ? stats.passed + " " + Icons.Passed : " ";
        }

        static String skipped(Stats stats) {
            return stats.skipped > 0 ? stats.skipped + " " + Icons.Skipped : " ";
        }

        static String failed(Stats stats) {
            return stats.failed > 0 ? stats.failed + " " + Icons.Failed : " ";
        }


        static String outcome(Stats stats) {
            return switch (stats.getStatus()) {
                case PASSED -> Passed;
                case SKIPPED -> Skipped;
                case FAILED -> Failed;
            };
        }
    }
}
