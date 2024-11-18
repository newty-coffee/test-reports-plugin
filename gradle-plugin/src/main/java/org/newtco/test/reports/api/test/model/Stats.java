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

package org.newtco.test.reports.api.test.model;

import java.util.Collection;

public class Stats {
    public Status status;
    public long   total;
    public long   passed;
    public long   skipped;
    public long   failed;
    public long   startTime;
    public long   endTime;
    public long   duration;

    public static Stats collect(Collection<? extends Stats> containers) {
        var combined = new Stats();
        for (var stats : containers) {
            // Once we have failed status, we don't change it
            if (combined.status != Status.FAILED) {
                combined.status = stats.status;
            }
            combined.total    = combined.total + stats.total;
            combined.passed   = combined.passed + stats.passed;
            combined.skipped  = combined.skipped + stats.skipped;
            combined.failed   = combined.failed + stats.failed;
            combined.duration = combined.endTime - combined.startTime;

            if (combined.startTime > 0 && stats.startTime > 0) {
                combined.startTime = Math.min(combined.startTime, stats.startTime);
            } else if (stats.startTime > 0) {
                combined.startTime = stats.startTime;
            }

            if (combined.endTime > 0 && stats.endTime > 0) {
                combined.endTime = Math.max(combined.endTime, stats.endTime);
            } else if (stats.endTime > 0) {
                combined.endTime = stats.endTime;
            }
        }
        return combined;
    }

    public Status getStatus() {
        return status;
    }

    public long getTotal() {
        return total;
    }

    public long getPassed() {
        return passed;
    }

    public long getSkipped() {
        return skipped;
    }

    public long getFailed() {
        return failed;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return duration;
    }
}