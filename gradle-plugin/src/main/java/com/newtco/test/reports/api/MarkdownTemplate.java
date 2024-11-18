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

package com.newtco.test.reports.api;

import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A template class for generating Markdown content. Custom templates inherit from this class.
 *
 * @param <T> The type parameter extending MarkdownTemplate
 */
public abstract class MarkdownTemplate<T extends MarkdownTemplate<T>> extends Template<T> {
    protected MarkdownTemplate(Writer writer) {
        super(writer);
    }

    public T h1(String... texts) {
        return text("# ").text(texts).eol();
    }

    public T h2(String... texts) {
        return out("## ").text(texts).eol();
    }

    public T h3(String... texts) {
        return out("### ").text(texts).eol();
    }

    public T hr() {
        return out("---").eol();
    }

    public T link(String title, String address) {
        return out("[", title, "](", address, ")");
    }

    public T image(String title, String src) {
        return out("![", title, "](", src, ")");
    }

    public T picture(String alt, String src) {
        return text("<picture><img alt=\"", alt, "\" src=\"", src, "\"></picture>");
    }

    public T bold(String... texts) {
        return out("<b>").text(texts).out("</b>");
    }

    public T italic(String... texts) {
        return out("<i>").text(texts).out("</i>");
    }

    public T code(String... texts) {
        return out("````").text(texts).out("````");
    }

    public T pre(String... texts) {
        return out("<pre>").text(texts).out("</pre>");
    }

    public T sup(String... texts) {
        return out("<sup>").text(texts).out("</sup>");
    }

    public T br() {
        return out("<br/>");
    }

    public T sub(String... texts) {
        return out("<sub>").text(texts).out("</sub>");
    }

    public T details(String summary, String... contents) {
        out("<details>\n",
            "<summary>", summary, "</summary>  \n");
        for (var content : contents) {
            out(content);
        }
        return out("  \n</details>\n");
    }

    public T comment(String... comments) {
        return out("<!-- ").text(comments).out(" -->");
    }

    public T line(String... texts) {
        return text(texts).eol();
    }

    public T eol() {
        out("  \n");
        return self();
    }

    public T text(String... texts) {
        return texts.length == 1
               ? out(texts[0])
               : out((Object[]) texts);
    }

    public <V> T repeat(Collection<V> values, Consumer<V> processor) {
        for (var value : values) {
            processor.accept(value);
        }
        return self();
    }

    public <V, U> T repeat(Collection<V> values, U context, BiConsumer<U, V> processor) {
        for (var value : values) {
            processor.accept(context, value);
        }
        return self();
    }

    public String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }


        var escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            int cp = value.codePointAt((i));
            switch (cp) {
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                default:

                    if (cp <= 31 || cp >= 127) {
                        escaped.append("&#").append(cp).append(';');
                    } else {
                        escaped.append((char) cp);
                    }
            }
        }

        return escaped.toString();
    }

    public String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        var escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            int cp = value.codePointAt((i));
            switch (cp) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '`':
                    escaped.append("\\`");
                    break;
                case '*':
                    escaped.append("\\*");
                    break;
                case '_':
                    escaped.append("\\_");
                    break;
                case '{':
                    escaped.append("\\{");
                    break;
                case '}':
                    escaped.append("\\}");
                    break;
                case '[':
                    escaped.append("\\[");
                    break;
                case ']':
                    escaped.append("\\]");
                    break;
                case '<':
                    escaped.append("\\<");
                    break;
                case '>':
                    escaped.append("\\>");
                    break;
                case '(':
                    escaped.append("\\(");
                    break;
                case ')':
                    escaped.append("\\)");
                    break;
                case '#':
                    escaped.append("\\#");
                    break;
                case '+':
                    escaped.append("\\+");
                    break;
                case '-':
                    escaped.append("\\-");
                    break;
                case '.':
                    escaped.append("\\.");
                    break;
                case '!':
                    escaped.append("\\!");
                    break;
                case '|':
                    escaped.append("\\|");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (cp <= 31 || cp >= 127) {
                        escaped.append("&#").append(cp).append(';');
                    } else {
                        escaped.append((char) cp);
                    }
            }
        }
        return escaped.toString();
    }

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
         * Converts a given timestamp in milliseconds to a formatted string in ISO_OFFSET_DATE_TIME format with a 'Z'
         * zone offset.
         *
         * @param result the timestamp in milliseconds to be converted
         *
         * @return a formatted string representation of the timestamp
         */
        public static String timestamp(long result) {
            return Instant.ofEpochMilli(result)
                .atOffset(ZoneOffset.of("Z"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        /**
         * Converts a given timestamp in milliseconds to a formatted string in ISO_OFFSET_DATE_TIME format for a
         * specified time zone.
         *
         * @param result the timestamp in milliseconds to be converted
         * @param zone   the time zone for formatting the timestamp (e.g., "Z" for UTC, "+02:00" for a specific offset,
         *               or a timezone ID like "America/New_York")
         *
         * @return a formatted string representation of the timestamp in the specified time zone
         */
        public static String timestamp(long result, String zone) {
            var timestamp = Instant.ofEpochMilli(result);

            if (zone.equals("Z") || zone.startsWith("+") || zone.startsWith("-")) {
                return timestamp.atOffset(ZoneOffset.of(zone))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else {
                return timestamp.atZone(ZoneId.of(zone))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        }

        /**
         * Calculates the duration between two time points specified by start and end timestamps.
         *
         * @param start the start timestamp in milliseconds
         * @param end   the end timestamp in milliseconds
         *
         * @return a formatted string representation of the duration, adjusted to the most appropriate time unit
         */
        public static String duration(long start, long end) {
            return duration(end - start);
        }

        /**
         * Formats the given duration into a human-readable string, adjusted to the most appropriate time unit.
         *
         * @param duration the duration in milliseconds to be formatted
         *
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
