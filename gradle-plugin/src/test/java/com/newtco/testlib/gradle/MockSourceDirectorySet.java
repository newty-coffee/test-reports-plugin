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

package com.newtco.testlib.gradle;

import java.io.File;
import java.util.Set;
import javax.annotation.Nonnull;

import org.gradle.api.file.SourceDirectorySet;
import org.mockito.Mockito;

import static com.newtco.testlib.gradle.MockUtil.asPath;

/**
 * MockSourceDirectorySet is a mock implementation of the SourceDirectorySet interface for testing purposes. It extends
 * MockFileTree to utilize its mock file tree structure and provides additional functionalities specific to source
 * directory sets.
 */
public abstract class MockSourceDirectorySet extends MockFileTree implements SourceDirectorySet {
    String name;

    public static MockSourceDirectorySet create(String name, Object dir, Object... files) {
        var mock = Mockito.spy(MockSourceDirectorySet.class);
        mock.dir = asPath(dir);
        mock.setFiles(files);
        mock.name = name;
        return mock;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Set<File> getSrcDirs() {
        return Set.of(dir.toFile());
    }
}