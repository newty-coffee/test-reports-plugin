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

public class AssertionFailure extends Failure {
    public String expected;
    public String actual;

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }

    @Override
    public String toString() {
        return new StringBuilder("AssertionFailure {")
            .append("type=").append(type)
            .append(", message='").append(message).append('\'')
            .append(", className='").append(className).append('\'')
            .append(", stackTrace='").append(stackTrace).append('\'')
            .append(", lineNumber=").append(lineNumber)
            .append(", causes=").append(causes)
            .append(", expected='").append(expected).append('\'')
            .append(", actual='").append(actual).append('\'')
            .append('}')
            .toString();
    }
}
