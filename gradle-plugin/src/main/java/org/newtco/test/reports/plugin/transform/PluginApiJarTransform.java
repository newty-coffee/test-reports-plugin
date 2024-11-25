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

package org.newtco.test.reports.plugin.transform;

import org.newtco.test.reports.api.Template;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipFile;

/**
 * Performs a transformation of plugin JAR files to generate an API-specific JAR. This is achieved by filtering the
 * contents of the input JAR and including only the classes that belong to the API packages.
 */
public abstract class PluginApiJarTransform implements TransformAction<PluginApiJarTransform.Parameters> {

    @Inject
    public PluginApiJarTransform() {
    }

    @InputArtifact
    @PathSensitive(PathSensitivity.NAME_ONLY)
    protected abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(@Nonnull TransformOutputs outputs) {
        var log = Logging.getLogger(PluginApiJarTransform.class);

        var pluginJarFile = getInputArtifact().get().getAsFile();
        if (!pluginJarFile.getName().endsWith(".jar")) {
            log.info("Ignoring non-JAR file: {}", pluginJarFile.getAbsolutePath());
            return;
        }

        var artifactName = getParameters().getArtifactName().get();
        var apiJarName   = pluginJarFile.getName().replace(artifactName, artifactName + "-api");
        var apiJarFile   = outputs.file(apiJarName);

        log.info("Transforming {} to {}", pluginJarFile.getName(), apiJarName);

        // Define the pattern to include only API classes. We'll use the Template class since it lives in the
        // root of the api package.
        var includePrefix = Template.class.getPackageName().replace('.', '/');

        try (var apiJar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(apiJarFile)))) {
            try (var zip = new ZipFile(pluginJarFile)) {

                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    var name  = entry.getName();
                    // Only add API related classes to the output JAR
                    if (!entry.isDirectory() && name.startsWith(includePrefix)) {
                        log.debug("Adding API class {}", getClassName(name));
                        apiJar.putNextEntry(new JarEntry(name));
                        zip.getInputStream(entry).transferTo(apiJar);
                        apiJar.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error transforming %s to %s".formatted(
                    pluginJarFile.getAbsolutePath(),
                    apiJarFile.getAbsolutePath()), e);
        }
    }

    private String getClassName(String name) {
        return name.substring(0, name.lastIndexOf('.'))
                .replace('/', '.')
                .replace('$', '.');
    }

    public interface Parameters extends TransformParameters {
        // Used during development to force the transform to be regenerated
        @Input
        Property<String> getTimestamp();

        @Input
        Property<String> getArtifactName();
    }
}
