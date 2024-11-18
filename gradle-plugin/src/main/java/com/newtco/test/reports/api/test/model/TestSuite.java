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

package com.newtco.test.reports.api.test.model;

import java.util.List;

public class TestSuite extends Stats {

    public int            id;
    public String         name;
    public String         displayName;
    public String         className;
    public String         stdErr;
    public String         stdOut;
    public List<TestCase> tests;


    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClassName() {
        return className;
    }

    public String getStdErr() {
        return stdErr;
    }

    public String getStdOut() {
        return stdOut;
    }

    public List<TestCase> getTests() {
        return tests;
    }

    @Override
    public String toString() {
        return new StringBuilder("TestSuite{")
            .append("total=").append(total)
            .append(", passed=").append(passed)
            .append(", skipped=").append(skipped)
            .append(", failed=").append(failed)
            .append(", startTime=").append(startTime)
            .append(", endTime=").append(endTime)
            .append(", duration=").append(duration)
            .append(", id=").append(id)
            .append(", status=").append(status)
            .append(", name='").append(name).append('\'')
            .append(", displayName='").append(displayName).append('\'')
            .append(", className='").append(className).append('\'')
            .append(", stdError='").append(stdErr).append('\'')
            .append(", stdOutput='").append(stdOut).append('\'')
            .append(", tests=").append(tests)
            .append('}')
            .toString();
    }
}
