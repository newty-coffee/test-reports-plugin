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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * The PluginAttributes class encapsulates the attributes of a plugin, such as its name, ID, group, artifact ID, and
 * version. These attributes are extracted from extra manifest entries added in the build file.
 */
class PluginProperties {
    private final String name;
    private final String id;
    private final String group;
    private final String artifact;
    private final String version;
    private final String timestamp;

    public PluginProperties(Attributes attributes) {
        this.name      = Objects.requireNonNull(attributes.getValue("plugin-name"));
        this.id        = Objects.requireNonNull(attributes.getValue("plugin-id"));
        this.group     = Objects.requireNonNull(attributes.getValue("plugin-group"));
        this.artifact  = Objects.requireNonNull(attributes.getValue("plugin-artifact"));
        this.version   = Objects.requireNonNull(attributes.getValue("plugin-version"));
        this.timestamp = Objects.requireNonNull(attributes.getValue("plugin-timestamp"));
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public String getCoordinates() {
        return group + ":" + artifact + ":" + version;
    }

    public boolean isSnapshot() {
        return version.endsWith("-SNAPSHOT");
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static PluginProperties load(String name) {
        try (var resource = PluginProperties.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {
            return new PluginProperties(new Manifest(resource).getAttributes(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
