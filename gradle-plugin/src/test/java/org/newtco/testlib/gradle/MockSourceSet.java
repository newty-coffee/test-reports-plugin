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

package org.newtco.testlib.gradle;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.newtco.testlib.gradle.MockUtil.*;

/**
 * MockSourceSet is an abstract class that implements the SourceSet interface and is used to create mock implementations
 * of source sets for testing purposes.
 */
public abstract class MockSourceSet implements SourceSet {
    String                 sourceSetName;
    MockSourceDirectorySet java;
    MockSourceDirectorySet resources;
    MockSourceDirectorySet allSource;
    MockSourceSetOutput    output;

    protected MockSourceSet() {
    }

    public static MockSourceSet create(String sourceSetName, String projectDir, Object... files) {
        var buildDir = Path.of(projectDir, "build");

        var mock = Mockito.spy(MockSourceSet.class);
        mock.sourceSetName = sourceSetName;
        mock.java          = MockSourceDirectorySet.create("java",
                asPath(projectDir, "src", sourceSetName, "java"),
                javaFiles(files));
        mock.resources     = MockSourceDirectorySet.create("resources",
                asPath(projectDir, "src", sourceSetName, "resources"),
                resourceFiles(files));

        mock.allSource = MockSourceDirectorySet.create("allSource",
                asPath(projectDir, "src", sourceSetName));
        var allSourceFiles = new ArrayList<File>();
        allSourceFiles.addAll(mock.java.getFiles());
        allSourceFiles.addAll(mock.resources.getFiles());
        mock.allSource.setFiles(allSourceFiles);

        mock.output = MockSourceSetOutput.create(sourceSetName, buildDir, outputFiles(files));

        return mock;
    }


    @Nonnull
    @Override
    public String getName() {
        return sourceSetName;
    }

    @Nonnull
    @Override
    public MockSourceSetOutput getOutput() {
        return output;
    }

    @Nonnull
    @Override
    public MockSourceDirectorySet getResources() {
        return resources;
    }

    @Nonnull
    @Override
    public MockSourceDirectorySet getJava() {
        return java;
    }

    @Nonnull
    @Override
    public MockSourceDirectorySet getAllJava() {
        return java;
    }

    @Nonnull
    @Override
    public SourceDirectorySet getAllSource() {
        return allSource;
    }
}
