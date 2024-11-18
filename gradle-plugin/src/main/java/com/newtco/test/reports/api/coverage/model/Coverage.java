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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;

/**
 * Represents coverage data with a specific type parameter that extends ICoverageNode.
 *
 * @param <T> the type of ICoverageNode
 */
public abstract class Coverage<T extends ICoverageNode> {
    protected final T        coverage;
    protected final Counters counters;

    protected Coverage(T coverage) {
        this.coverage = coverage;
        this.counters = new Counters();
    }

    /**
     * Get the underlying ICoverageNode
     */
    public T getCoverage() {
        return coverage;
    }

    public ICoverageNode.ElementType getElementType() {
        return coverage.getElementType();
    }

    public boolean containsCode() {
        return coverage.containsCode();
    }

    public Counters getCounters() {
        return counters;
    }

    public class Counters {

        public Counter getInstructions() {
            return new Counter(coverage.getInstructionCounter());
        }

        public Counter getBranches() {
            return new Counter(coverage.getBranchCounter());
        }

        public Counter getLines() {
            return new Counter(coverage.getLineCounter());
        }

        public Counter getComplexity() {
            return new Counter(coverage.getComplexityCounter());
        }

        public Counter getMethods() {
            return new Counter(coverage.getMethodCounter());
        }

        public Counter getClasses() {
            return new Counter(coverage.getClassCounter());
        }

    }

    /**
     * The Counter class provides an abstraction over the ICounter interface, offering additional functionality and
     * textual representations of different coverage states.
     */
    public static class Counter {
        private static final String[] StatusText = new String[]{
            "EMPTY", "NOT_COVERED", "FULLY_COVERED", "PARTLY_COVERED",
        };
        private final        ICounter counter;

        public Counter(ICounter counter) {
            this.counter = counter;
        }

        public ICounter getCounter() {
            return counter;
        }

        public int getTotalCount() {
            return counter.getTotalCount();
        }

        public int getCoveredCount() {
            return counter.getCoveredCount();
        }

        public int getMissedCount() {
            return counter.getMissedCount();
        }

        public int getStatus() {
            return counter.getStatus();
        }

        /**
         * Returns the textual representation of the counter's status.
         *
         * @return a string representing the status of the counter, such as "EMPTY", "NOT_COVERED", "FULLY_COVERED",
         * "PARTLY_COVERED" or "UNKNOWN" if the status code is out of range.
         */
        public String getStatusText() {
            int status = counter.getStatus();
            return status > 0 && status < StatusText.length
                   ? StatusText[status]
                   : "UNKNOWN";
        }

        /**
         * Calculates and returns the percentage of covered items.
         *
         * @return the percentage of covered items, ranging from 0.0 to 100.0
         */
        public double getPercentCovered() {
            var total = counter.getCoveredCount() + counter.getMissedCount();
            if (total == 0) {
                return 0.0f;
            }

            double percentage = 100.0f * ((double) counter.getCoveredCount() / total);
            if (percentage > 100.0f) {
                percentage = 100.0f;
            }
            return percentage;
        }

        /**
         * Calculates and returns the percentage of missed items.
         *
         * @return the percentage of missed items, ranging from 0.0 to 100.0
         */
        public double getPercentMissed() {
            var total = counter.getCoveredCount() + counter.getMissedCount();
            if (total == 0) {
                return 0.0f;
            }

            double percentage = 100.0f * ((double) counter.getMissedCount() / total);
            if (percentage > 100.0f) {
                percentage = 100.0f;
            }
            return percentage;
        }
    }
}
