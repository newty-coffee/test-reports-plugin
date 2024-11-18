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

public class Failure {
    public FailureType   type;
    public String        message;
    public String        className;
    public String        stackTrace;
    public int           lineNumber;
    public List<Failure> causes;

    public FailureType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getClassName() {
        return className;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public List<Failure> getCauses() {
        return causes;
    }

    public boolean isAssertion() {
        return type == FailureType.ASSERTION;
    }

    public boolean isFileComparison() {
        return type == FailureType.FILE;
    }

    public boolean isException() {
        return type == FailureType.EXCEPTION;
    }


    @Override
    public String toString() {
        return new StringBuilder("Failure {")
            .append("type=").append(type)
            .append(", message='").append(message).append('\'')
            .append(", className='").append(className).append('\'')
            .append(", stackTrace='").append(stackTrace).append('\'')
            .append(", lineNumber=").append(lineNumber)
            .append(", causes=").append(causes)
            .append('}')
            .toString();
    }
}

