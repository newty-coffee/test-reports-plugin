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

package com.newtco.test.util;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class providing common text operations
 */
public class Text {

    private Text() {
    }

    /**
     * Checks if the given string is either null or empty.
     *
     * @param value the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Checks if the given string is either null or blank.
     *
     * @param value the string to check
     * @return true if the string is null or blank, false otherwise
     */
    public static boolean isBlank(String value) {
        return null == value || value.isBlank();
    }

    /**
     * Utility class for formatting operations.
     */
    public static class Format {

        private static final Map<TimeUnit, String> SUFFIXES = new EnumMap<>(Map.of(
                TimeUnit.HOURS, "h",
                TimeUnit.MINUTES, "m",
                TimeUnit.SECONDS, "s",
                TimeUnit.MILLISECONDS, "ms"
        ));

        private Format() {
        }

        /**
         * Calculates the duration between two time points specified by start and end timestamps.
         *
         * @param start the start timestamp in milliseconds
         * @param end   the end timestamp in milliseconds
         * @return a formatted string representation of the duration, adjusted to the most appropriate time unit
         */
        public static String duration(long start, long end) {
            return duration(end - start);
        }

        /**
         * Computes the duration between two time points denoted by `start` and `end` Instants.
         *
         * @param start the start instant
         * @param end   the end instant
         * @return a formatted string representation of the duration between the two instants
         */
        public static String duration(Instant start, Instant end) {
            return duration(Duration.between(start, end));
        }


        /**
         * Converts the given Duration object into a formatted string representation.
         *
         * @param duration the Duration object to be formatted
         * @return a formatted string representation of the duration
         */
        public static String duration(Duration duration) {
            long millis = duration.toMillis();
            return duration(millis);
        }

        /**
         * Formats the given duration into a human-readable string, adjusted to the most appropriate time unit.
         *
         * @param duration the duration in milliseconds to be formatted
         * @return a formatted string representation of the duration
         */
        public static String duration(long duration) {
            var unit = getNearestUnit(duration);

            var value = duration / (double) TimeUnit.MILLISECONDS.convert(1, unit);
            var text  = "%.3f".formatted(value);
            if (text.endsWith("000")) {
                text = text.substring(0, text.length() - 3);
            } else if (text.endsWith("00")) {
                text = text.substring(0, text.length() - 2);
            } else if (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }

            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
            return text + SUFFIXES.get(unit);
        }

        private static TimeUnit getNearestUnit(long duration) {
            if (duration >= 3_600_000) {
                return TimeUnit.HOURS;
            }
            if (duration >= 60_000L) {
                return TimeUnit.MINUTES;
            }
            if (duration >= 1000L) {
                return TimeUnit.SECONDS;
            }
            return TimeUnit.MILLISECONDS;
        }
    }
}



