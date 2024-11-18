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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.LoggingManager;

/**
 * Runs the JDK JDeps ToolProvider on a set of classes to extract their dependencies (imports)
 */
public class JdepsDependencyCollector {

    private final Logger log;

    public JdepsDependencyCollector() {
        this.log = Logging.getLogger(JdepsDependencyCollector.class);
    }

    /**
     * Creates a BinaryOperator<String> that maps a full class name to the class name prefixed with the specified
     * prefix.
     *
     * @param prefix the prefix to match against the full class name
     *
     * @return a BinaryOperator<String> that returns the class name if it matches the prefix, or null otherwise
     */
    public static BinaryOperator<String> classNameMapper(String prefix) {
        return (outerName, fullName) -> fullName.startsWith(prefix) ? fullName : null;
    }

    /**
     * Creates a BinaryOperator<String> that maps a full class name to the outer class name if it starts with the
     * specified prefix.
     *
     * @param prefix the prefix to match against the outer class name
     *
     * @return a BinaryOperator<String> that returns the outer class name if it starts with the prefix, or null
     * otherwise
     */
    public static BinaryOperator<String> outerClassNameMapper(String prefix) {
        return (outerName, fullName) -> outerName.startsWith(prefix) ? outerName : null;
    }

    /**
     * Coverts a fully qualified class name to its outer class name
     *
     * @param name Class name in dotted form
     */
    private static String outerQualifiedName(String name) {
        // if nested class
        int term = name.lastIndexOf('.');
        if (term != -1) {
            term = name.indexOf('$', term + 1);
            if (term != -1) {
                return name.substring(0, term);
            }
        }
        return name;
    }

    /**
     * Retrieves all class dependencies of the specified class files.
     *
     * @param classes List of class files to inspect
     * @param mapper  Mapping function which receives an outer class name and a full class name, and returns the desired
     *                output class name or null to ignore it.
     */
    public Set<String> getDependencies(Collection<File> classes, BinaryOperator<String> mapper) {
        if (classes.isEmpty()) {
            return Set.of();
        }

        var jdeps = runJdeps(classes);

        // jdeps output looks like:
        //   source.class   ->   dependency  module.name
        //
        // The way we call it, it won't be able to resolve module names for any classes
        // that aren't core java modules, so they'll all look like this:
        //
        //   MyClassTests.class  ->  com.newtco.test.MyClass   not found
        //
        return jdeps.lines()
            .filter(line -> line.startsWith("  ") && line.endsWith("not found"))
            .map(line -> {
                int start = line.indexOf("-> ");
                if (start != -1) {
                    int end = line.indexOf(" not found", start + "-> ".length());
                    if (end != -1) {
                        var className = line.substring(start + "-> ".length(), end).trim();
                        return mapper.apply(outerQualifiedName(className), className);
                    }
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * Runs the jdeps tool on a collection of class files and returns its output.
     *
     * @param classFiles A collection of class files to analyze with the jdeps tool.
     *
     * @return The output of the jdeps tool as a String. Returns an empty string if the jdeps tool is not found or an
     * error occurs.
     */
    @Nonnull
    String runJdeps(Collection<File> classFiles) {
        var jdeps = ToolProvider.findFirst("jdeps").orElse(null);
        if (jdeps == null) {
            return "";
        }

        try (var out = new ByteArrayOutputStream(); var err = new ByteArrayOutputStream()) {
            var args = new ArrayList<String>();
            args.add("-verbose");
            args.addAll(classFiles.stream()
                .map(File::getPath)
                .toList());

            log.lifecycle(
                "Executing jdeps {}",
                String.join(" ", args));

            var exitCode = jdeps.run(
                new PrintWriter(out, true, StandardCharsets.UTF_8),
                new PrintWriter(err, true, StandardCharsets.UTF_8),
                args.toArray(String[]::new));
            if (exitCode != 0) {
                var error = err.toString(StandardCharsets.UTF_8);
                log.error("Failed to execute jdeps: Error: {}\n{}",
                    exitCode,
                    error);
            }

            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error running {}. {}", jdeps.name(), e.getMessage(), e);

            return "";
        }
    }
}



