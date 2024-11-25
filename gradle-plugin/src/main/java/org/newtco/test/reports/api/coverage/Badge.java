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

package org.newtco.test.reports.api.coverage;

import org.newtco.test.reports.api.coverage.model.Coverage;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Represents a badge with a style and color scheme, used to display coverage percentages. Badges are generated via
 * shields.io.
 */
public class Badge {
    private final String              style;
    private final ColorScheme.Color[] colors;

    /**
     * Constructs a BadgeGenerator with the specified style and color scheme. The colors are sorted based on their
     * thresholds in descending order.
     *
     * @param style  the style of the badge
     * @param colors an array of ColorScheme.Color representing the color scheme
     */
    public Badge(String style, ColorScheme.Color[] colors) {
        this.style  = style;
        this.colors = Stream.of(colors)
                .sorted(Comparator.comparingDouble(ColorScheme.Color::getThreshold).reversed())
                .toArray(ColorScheme.Color[]::new);
    }

    /**
     * Generates a coverage badge from the given coverage counter.
     *
     * @param counter the counter containing the coverage statistics
     * @return a string representing the generated badge with a style and color or just the percentage if the style is
     * "none"
     */
    public String create(Coverage.Counter counter) {
        return create(counter.getPercentCovered());
    }

    /**
     * Creates a formatted percentage string and, depending on the style, may generate a badge URL.
     *
     * @param percentage the percentage to be formatted
     * @return a string representing the formatted percentage. If the style is not "none", a badge URL string is
     * returned.
     */
    public String create(double percentage) {
        return create(percentage, style, getColor(percentage));
    }

    public String create(double percentage, String style, String color) {
        var formatted = "";
        if (percentage <= 0.00) {
            formatted = "0%";
        } else if (percentage >= 100.0f) {
            formatted = "100%";
        } else {
            formatted = String.format("%.1f%%", percentage);
        }

        if ("none".equals(style)) {
            return formatted;
        }

        // formatted ends with '%' which is the escape character, so add the "25" to turn it into the escape sequence for "%"
        return "![" + formatted + "](" + "https://img.shields.io/badge/" + formatted + "25-" + color + "?style=" + style + ")";
    }

    private String getColor(double percentage) {
        for (var color : colors) {
            if (percentage >= color.getThreshold()) {
                return color.getColor();
            }
        }

        return colors[colors.length - 1].getColor();
    }

    /**
     * The ColorScheme class represents a scheme of colors used for certain thresholds. It contains a predefined name
     * and an array of Color objects.
     */
    public static class ColorScheme {
        private final String  name;
        private final Color[] colors;

        /**
         * Constructs a new ColorScheme with the specified name and array of colors.
         *
         * @param name   the name of the color scheme
         * @param colors an array of Color objects associated with the scheme
         */
        public ColorScheme(String name, Color... colors) {
            this.name   = name;
            this.colors = colors;
        }

        /**
         * Creates a new ColorScheme instance with the given name and array of colors.
         *
         * @param name   the name of the color scheme
         * @param colors an array of Color objects associated with the scheme
         * @return a new ColorScheme instance
         */
        public static ColorScheme of(String name, Color... colors) {
            return new ColorScheme(name, colors);
        }

        /**
         * Retrieves the name of the ColorScheme.
         *
         * @return the name of the color scheme
         */
        public String getName() {
            return name;
        }

        /**
         * Retrieves the array of Color objects associated with this ColorScheme.
         *
         * @return an array of Color objects associated with the scheme
         */
        public Color[] getColors() {
            return colors;
        }

        /**
         * The Color class represents a color scheme with a specific threshold value. It associates a double threshold
         * with a corresponding color string. This can be used for various applications like categorizing grades,
         * levels, or other metrics based on color codes.
         */
        public static class Color {
            private final double threshold;
            private final String color;

            /**
             * Constructs a Color with the specified threshold and color code.
             *
             * @param threshold the threshold value associated with the color
             * @param color     the color code as a string
             */
            public Color(Double threshold, String color) {
                this.threshold = threshold;
                this.color     = color;
            }

            /**
             * Creates a new Color instance with the specified threshold and color.
             *
             * @param threshold the threshold value associated with the color
             * @param color     the color code as a string
             * @return a new Color instance
             */
            public static Color of(double threshold, String color) {
                return new Color(threshold, color);
            }

            /**
             * Retrieves the threshold value associated with the color.
             *
             * @return the threshold value
             */
            public double getThreshold() {
                return threshold;
            }

            /**
             * Retrieves the color code as a string.
             *
             * @return the color code
             */
            public String getColor() {
                return color;
            }
        }
    }
}
