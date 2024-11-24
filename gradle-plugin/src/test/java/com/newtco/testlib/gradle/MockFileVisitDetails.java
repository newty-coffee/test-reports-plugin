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

import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.RelativePath;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.Random;

import static com.newtco.testlib.gradle.MockUtil.asPath;

/**
 * A mock implementation of the FileVisitDetails interface for simulating file visit operations in tests.
 */
public abstract class MockFileVisitDetails implements FileVisitDetails {
    private RelativePath relativePath;
    private Path         filePath;
    private boolean      isDirectory;
    private boolean      stopVisiting;

    protected MockFileVisitDetails() {
    }

    // Mock API
    public static MockFileVisitDetails create(Object parent, Object file, boolean isDirectory) {
        return Mockito.spy(MockFileVisitDetails.class)
                .init(asPath(parent), asPath(file), isDirectory);
    }

    private MockFileVisitDetails init(Path parent, Path file, boolean isDirectory) {
        this.relativePath = RelativePath.parse(!isDirectory, parent.relativize(file).toString());
        this.filePath     = parent.resolve(file);
        this.isDirectory  = isDirectory;
        return this;
    }

    @Override
    public void stopVisiting() {
        stopVisiting = true;
    }

    @Nonnull
    @Override
    public File getFile() {
        return filePath.toFile();
    }

    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public long getLastModified() {
        var file = getFile();
        return file.exists()
                ? file.lastModified()
                : System.currentTimeMillis();
    }

    @Override
    public long getSize() {
        var file = getFile();
        return file.exists()
                ? file.length()
                : isDirectory
                ? 0
                : new Random().nextLong(1024 * 1024 * 1024);
    }

    @Nonnull
    @Override
    public String getName() {
        return filePath.getFileName().toString();
    }

    @Nonnull
    @Override
    public String getPath() {
        return filePath.toString();
    }

    @Nonnull
    @Override
    public RelativePath getRelativePath() {
        return relativePath;
    }

    // Mock API
    boolean isStopVisiting() {
        return stopVisiting;
    }
}
