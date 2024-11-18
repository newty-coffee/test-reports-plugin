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

package com.newtco.test.reports.plugin;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

/**
 * Transform attribute used to convert the full plugin JAR to its API only jar
 */
public interface PluginJarType extends Named {
    /**
     * The attribute that represents type of plugin jar file
     */
    Attribute<String> PLUGIN_JAR_TYPE_ATTRIBUTE = Attribute.of(PluginJarType.class.getName(),
        String.class);

    /**
     * Represents the full plugin JAR file.
     */
    String PLUGIN = "plugin";

    /**
     * Represents the API only plugin JAR file.
     */
    String API = "api";
}
