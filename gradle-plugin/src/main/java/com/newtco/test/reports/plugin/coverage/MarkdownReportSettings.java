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

package com.newtco.test.reports.plugin.coverage;

import com.newtco.test.reports.api.coverage.Badge;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

/**
 * Settings specific to generating Markdown reports. It extends the general report settings provided by the
 * {@link ReportSettings} class and includes additional configurations such as badge style and color schemes.
 */
public abstract class MarkdownReportSettings extends ReportSettings {

    public static final String DEFAULT_BADGE_STYLE  = "flat";
    public static final String DEFAULT_COLOR_SCHEME = "green-red";

    @Inject
    public MarkdownReportSettings(String name, Provider<RegularFile> reportFile) {
        super(name, reportFile);

        getAbbreviatePackages().convention(true);
        getBadgeStyle().convention(DEFAULT_BADGE_STYLE);
        getColorScheme().convention(DEFAULT_COLOR_SCHEME);
    }

    /**
     * Built in color schemes for coverage reports
     */
    static Map<String, Badge.ColorScheme> getColorSchemes() {
        return Map.of(
                DEFAULT_COLOR_SCHEME,
                new Badge.ColorScheme(DEFAULT_COLOR_SCHEME,
                        Badge.ColorScheme.Color.of(100.0f, "4caf50"),
                        Badge.ColorScheme.Color.of(80.0f, "88bc4b"),
                        Badge.ColorScheme.Color.of(60.0f, "ffeb3b"),
                        Badge.ColorScheme.Color.of(50.0f, "ce5226"),
                        Badge.ColorScheme.Color.of(0.0f, "9b0000")
                ),
                "monochrome",
                new Badge.ColorScheme(DEFAULT_COLOR_SCHEME,
                        Badge.ColorScheme.Color.of(100.0f, "212121"),
                        Badge.ColorScheme.Color.of(80.0f, "616161"),
                        Badge.ColorScheme.Color.of(60.0f, "9e9e9e"),
                        Badge.ColorScheme.Color.of(50.0f, "bdbdbd"),
                        Badge.ColorScheme.Color.of(0.0f, "e0e0e0")
                ),
                "blue-red",
                new Badge.ColorScheme(DEFAULT_COLOR_SCHEME,
                        Badge.ColorScheme.Color.of(100.0f, "0d47a1"),
                        Badge.ColorScheme.Color.of(80.0f, "1976d2"),
                        Badge.ColorScheme.Color.of(60.0f, "1e88e5"),
                        Badge.ColorScheme.Color.of(50.0f, "d32f2f"),
                        Badge.ColorScheme.Color.of(0.0f, "c62828")
                )
        );
    }

    /**
     * Determines whether package names are abbreviated in the generated Markdown report.
     *
     * @return a Property representing whether package names should be abbreviated.
     */
    @Input
    public abstract Property<Boolean> getAbbreviatePackages();

    /**
     * Retrieves the style of the badge to be used in the Markdown report.
     *
     * @return a Property representing the badge style.
     * @see https://shields.io
     */
    @Input
    public abstract Property<String> getBadgeStyle();

    /**
     * Retrieves the color scheme to be applied in the Markdown report. Can be {@code green-red}, {@code monochrome},
     * {@code blue-red}.
     *
     * @return a Property representing the color scheme.
     */
    @Input
    public abstract Property<String> getColorScheme();

    /**
     * Retrieves the current color scheme being used in the Markdown report settings.
     * <p>
     * The method checks the current color scheme name against the available color schemes. If the color scheme name
     * does not exist in the available schemes, an exception is thrown.
     *
     * @return the ColorScheme associated with the current color scheme name.
     * @throws GradleException if the color scheme name is not found in the available color schemes.
     */
    @Internal
    public Badge.ColorScheme getCurrentColorScheme() {
        var name    = getColorScheme().getOrElse(DEFAULT_COLOR_SCHEME);
        var schemes = getColorSchemes();

        if (!schemes.containsKey(name)) {
            throw new GradleException("Scheme name '%s' not found. Must be one of: %s".formatted(
                    name,
                    String.join(", ", schemes.keySet())));
        }

        return schemes.get(name);
    }
}
