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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.newtco.test.reports.api.test.model.Stats;
import com.newtco.test.reports.api.test.model.Status;
import com.newtco.test.reports.api.test.model.TestSuite;

@SuppressWarnings("UnstableApiUsage")
public abstract class TestReport {


    protected static final String HOSTNAME = getHostName();

    public abstract void generateReport(List<TestSuite> suites);

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return InetAddress.getLoopbackAddress().getHostAddress();
        }
    }

    protected static Stats accumulatedMetrics(List<TestSuite> suites) {
        var metrics = new Stats();
        for (var suite : suites) {
            metrics.total     = metrics.total + suite.total;
            metrics.passed    = metrics.passed + suite.passed;
            metrics.skipped   = metrics.skipped + suite.skipped;
            metrics.failed    = metrics.failed + suite.failed;
            metrics.startTime = metrics.startTime > 0
                                ? Math.min(metrics.startTime, suite.startTime)
                                : suite.startTime;
            metrics.endTime   = Math.max(metrics.endTime, suite.endTime);
            metrics.duration  = metrics.endTime - metrics.startTime;
        }
        return metrics;
    }

    protected static class Options {

        protected final Set<Status> statuses;
        protected final String      fileExtension;
        protected final Path             outputDir;

        Options(
            ReportSettings settings,
            String fileExtension,
            Path outputDir) {
            this.statuses      = settings.getTestOutcomes().map(EnumSet::copyOf).get();
            this.fileExtension = fileExtension;
            this.outputDir     = outputDir;
        }

        public boolean isReportable(Status status) {
            return statuses.contains(status);
        }

        public Path resolveReportFile(String name) {
            var fileName = new StringBuilder("TEST-");
            for (char ch : name.toCharArray()) {
                if ((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9')
                    || (ch == '_')
                    || (ch == '-')
                    || (ch == '.')
                    || (ch == '$')) {
                    fileName.append(ch);
                } else {
                    fileName.append('#')
                        .append(Integer.toHexString(ch));
                }
            }
            fileName.append(fileExtension);

            return outputDir.resolve(fileName.toString());
        }

    }
}



