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
        return "AssertionFailure {" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", className='" + className + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", lineNumber=" + lineNumber +
                ", causes=" + causes +
                ", expected='" + expected + '\'' +
                ", actual='" + actual + '\'' +
                '}';
    }
}
