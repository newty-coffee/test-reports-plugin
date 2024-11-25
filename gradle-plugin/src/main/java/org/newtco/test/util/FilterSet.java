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

package org.newtco.test.util;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The {@code FilterSet} class allows for managing include and exclude string patterns with an optional case sensitivity
 * feature. Patterns support ant-style matching which can be converted to regular expressions.
 * <p>
 * This is a simpler version of {@code org.gradle.api.tasks.util.PatternSet}
 */
public class FilterSet {

    private final Set<String> includes;
    private final Set<String> excludes;
    private       boolean     caseSensitive;

    @Inject
    public FilterSet() {
        this.includes      = new LinkedHashSet<>();
        this.excludes      = new LinkedHashSet<>();
        this.caseSensitive = false;
    }


    public Set<String> getIncludes() {
        return includes;
    }

    public void include(Object... values) {
        for (var value : values) {
            var include = Objects.toString(value, null);
            if (include != null) {
                includes.add(caseSensitive
                        ? include.toLowerCase()
                        : include);
            }
        }
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void exclude(Object... values) {
        for (var value : values) {
            var exclude = Objects.toString(value, null);
            if (exclude != null) {
                excludes.add(caseSensitive
                        ? exclude.toLowerCase()
                        : exclude);
            }
        }
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    public Predicate<String> asPredicate() {
        if (includes.isEmpty() && excludes.isEmpty()) {
            return (unused) -> true;
        }

        var includesRgx = includes.isEmpty()
                ? null
                : Pattern.compile(antToRegex(includes), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);

        var excludesRgx = excludes.isEmpty()
                ? null
                : Pattern.compile(antToRegex(excludes), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);

        return (value) -> {
            // The rules are:
            //  - If excludes is not empty, it must not contain the value
            //  - If includes is not empty, it must contain the value

            if (excludesRgx != null && excludesRgx.matcher(value).matches()) {
                return false;
            }

            return includesRgx == null || includesRgx.matcher(value).matches();
        };
    }

    private String antToRegex(Set<String> patterns) {
        return patterns.stream()
                .map(pattern -> {
                            var regex = new StringBuilder();
                            for (int i = 0, j = 1, k = pattern.length(); i < k; i++, j = i + 1) {
                                char ch = pattern.charAt(i);
                                switch (ch) {
                                    case '.':
                                    case '$':
                                        regex.append('\\').append(ch);
                                        break;
                                    case '?':
                                        regex.append('.');
                                        break;
                                    case '*':
                                        if (j < k && pattern.charAt(j) == '*') {
                                            regex.append(".*");
                                        } else {
                                            regex.append("[^.]*");
                                        }
                                        break;
                                    default:
                                        regex.append(ch);
                                        break;
                                }
                            }

                            return regex.toString();
                        }
                )
                .collect(Collectors.joining("|", "^(", ")$"));
    }
}
