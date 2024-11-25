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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FileFailure extends Failure {
    private static final int    MAX_BINARY_LENGTH = 40;
    private static final char[] HexChars          = "0123456789ABCDEF".toCharArray();

    public String expectedPath;
    public byte[] expectedData;
    public String actualPath;
    public byte[] actualData;

    public String getExpectedPath() {
        return expectedPath;
    }

    public byte[] getExpectedData() {
        return expectedData;
    }

    public String getExpected() {
        return asText(expectedData, MAX_BINARY_LENGTH);
    }

    public String getExpected(int limit) {
        return asText(expectedData, limit);
    }

    public String getActualPath() {
        return actualPath;
    }

    public byte[] getActualData() {
        return actualData;
    }

    public String getActual() {
        return asText(actualData, MAX_BINARY_LENGTH);
    }

    public String getActual(int limit) {
        return asText(actualData, limit);
    }

    private String asText(byte[] data, int limit) {
        if (data == null) {
            return "null";
        }

        if (data.length == 0) {
            return "<empty>";
        }

        var binary = new StringBuilder();
        if (data.length > limit) {
            binary.append("[").append(data.length).append(" bytes]: ");
        }

        if (isTextContent(data, limit)) {
            binary.append(new String(data, 0, Math.min(data.length, limit), StandardCharsets.US_ASCII));
        } else {
            for (int i = 0; i < limit; i++) {
                int b = data[i] & 0xFF;
                binary.append(HexChars[b >>> 4]);
                binary.append(HexChars[b & 0x0F]);
            }
        }

        if (data.length > limit) {
            binary.append("...");
        }

        return binary.toString();
    }

    private boolean isTextContent(byte[] data, int limit) {
        int length = Math.min(data.length, limit);
        for (int i = 0; i < length; i++) {
            byte b = data[i];
            if (b == 0) {
                return false;
            }
            if (b < 32 && b != '\t' && b != '\r' && b != '\n') {
                return false;
            }
        }
        return true;
    }


    @Override
    public String toString() {
        return "FileFailure {" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", className='" + className + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", lineNumber=" + lineNumber +
                ", causes=" + causes +
                ", expectedPath='" + expectedPath + '\'' +
                ", expectedData=" + Arrays.toString(expectedData) +
                ", actualPath='" + actualPath + '\'' +
                ", actualData=" + Arrays.toString(actualData) +
                '}';
    }
}