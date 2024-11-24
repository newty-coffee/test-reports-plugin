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

import java.util.Objects;

/**
 * Utility class to create dynamic URL builders based on a provided template.
 * <p>
 * The template can contain placeholders for `repository`, `commit`, and `file` which will be substituted dynamically
 * when the URL is built.
 */
public class GitLinkTemplate {

    /**
     * Parses a URL template and creates a UrlBuilder instance that can populate the template with dynamic values.
     *
     * @param template the URL template containing fixed URL parts and variables enclosed in braces (e.g., {repository},
     *                 {commit}, {file}). Variables will be replaced by their corresponding values when invoking the
     *                 build method on the UrlBuilder.
     * @return a UrlBuilder instance capable of generating URLs based on the template and provided values for variables.
     */
    public static GitLinkBuilder createLinkBuilder(String template) {
        Builder head   = null;
        int     length = template.length();
        int     i      = 0;

        while (i < length) {
            int start = template.indexOf('{', i);
            if (start == -1) {
                // Append the remaining literal part
                var literalPart    = template.substring(i);
                var literalBuilder = (Builder) (sb, repository, commit, file) -> sb.append(literalPart);

                head = head == null
                        ? literalBuilder
                        : head.andThen(literalBuilder);
                break;
            }

            if (start > i) {
                // Add the literal part before the '{'
                var literalPart    = template.substring(i, start);
                var literalBuilder = (Builder) (sb, repository, commit, file) -> sb.append(literalPart);
                head = head == null
                        ? literalBuilder
                        : head.andThen(literalBuilder);
            }

            int end = template.indexOf('}', start);
            if (end == -1) {
                throw new IllegalArgumentException("Unmatched '{' in URL template '" + template + "'");
            }

            // Extract variable name between '{' and '}'
            var variable = template.substring(start + 1, end);
            var variableFunction = switch (variable) {
                case "repository" -> (Builder) (sb, repository, commit, file) -> sb.append(repository);
                case "commit" -> (Builder) (sb, repository, commit, file) -> sb.append(commit);
                case "file" -> (Builder) (sb, repository, commit, file) -> sb.append(file);
                default ->
                        throw new IllegalArgumentException("Unknown URL template variable: '" + variable + "'. Must be one of 'repository', 'commit', or 'file'.");
            };

            head = head == null
                    ? variableFunction
                    : head.andThen(variableFunction);

            i = end + 1;
        }

        var builder = Objects.requireNonNull(head);
        return (repository, commit, file) -> builder.apply(new StringBuilder(), repository, commit, file).toString();
    }


    @FunctionalInterface
    public interface GitLinkBuilder {
        String build(String repository, String commit, String file);
    }

    @FunctionalInterface
    private interface Builder {
        StringBuilder apply(StringBuilder sb, String repository, String commit, String file);

        default Builder andThen(Builder after) {
            return (sb, repository, commit, file) -> {
                return after.apply(apply(sb, repository, commit, file), repository, commit, file);
            };
        }
    }
}
