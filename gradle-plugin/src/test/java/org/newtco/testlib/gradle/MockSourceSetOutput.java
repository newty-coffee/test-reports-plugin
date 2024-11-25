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

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.SourceSetOutput;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;

import static org.newtco.testlib.gradle.MockUtil.*;

/**
 * An abstract class representing the output of a source set in a mock testing environment. This class extends
 * MockFileTree and implements the SourceSetOutput interface.
 */
public abstract class MockSourceSetOutput extends MockFileTree implements SourceSetOutput {
    MockFileTree classes;
    MockFileTree resources;

    public static MockSourceSetOutput create(String sourceSetName, Object dir, Object... files) {
        var mock = Mockito.spy(MockSourceSetOutput.class);
        mock.dir = asPath(dir);
        // Setup classes & resources first
        mock.classes   = MockFileTree.create(
                asPath(dir, "classes", "java", sourceSetName),
                classFiles(files));
        mock.resources = MockFileTree.create(
                asPath(dir, "resources", sourceSetName),
                resourceFiles(files));

        var outputs = new ArrayList<>();
        outputs.addAll(mock.classes.getFiles());
        outputs.addAll(mock.resources.getFiles());
        mock.setFiles(outputs);

        return mock;
    }

    @Nonnull
    @Override
    public FileTree getAsFileTree() {
        return this;
    }

    @Nonnull
    @Override
    public FileCollection getClassesDirs() {
        return classes;
    }

    @Nullable
    @Override
    public File getResourcesDir() {
        return asPath(dir, "resources").toFile();
    }

    @Nonnull
    @Override
    public FileCollection getDirs() {
        return this;
    }
}
