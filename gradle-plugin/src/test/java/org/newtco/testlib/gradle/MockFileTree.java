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

import org.gradle.api.Action;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.newtco.testlib.gradle.MockUtil.asPath;

/**
 * A mock implementation of a file tree for testing purposes. This class allows for the creation and manipulation of a
 * mock file tree structure that can be used to simulate file system operations in tests.
 */
public abstract class MockFileTree implements FileTree {
    Path       dir;
    List<Path> files;


    public static MockFileTree create(Object dir) {
        return create(dir, List.of());
    }

    public static MockFileTree create(Object dir, Object... files) {
        var mock = Mockito.spy(MockFileTree.class);
        mock.dir = asPath(dir);
        mock.setFiles(List.of(files));
        return mock;
    }

    @Nonnull
    @Override
    public FileTree visit(@Nonnull FileVisitor visitor) {
        for (var file : MockUtil.asFileTree(dir, files)) {
            if (file.endsWith("/")) {
                visitor.visitDir(MockFileVisitDetails.create(dir, file, true));
            } else {
                visitor.visitFile(MockFileVisitDetails.create(dir, file, false));
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public FileTree visit(@Nonnull Action<? super FileVisitDetails> visitor) {
        return visit(new FileVisitor() {

            @Override
            public void visitDir(@Nonnull FileVisitDetails dirDetails) {
                visitor.execute(dirDetails);
            }

            @Override
            public void visitFile(@Nonnull FileVisitDetails fileDetails) {
                visitor.execute(fileDetails);
            }
        });
    }

    @Nonnull
    @Override
    public FileTree getAsFileTree() {
        return this;
    }

    @Nonnull
    @Override
    public Set<File> getFiles() {
        return files.stream()
                // Differs from FileCollection in that all files have a shared root
                .map(file -> dir.resolve(file))
                .map(Path::toFile)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setFiles(Collection<?> files) {
        this.files = files.stream()
                .map(MockUtil::asPath)
                .sorted(Comparator.comparing(Path::toString))
                .toList();
    }

    public void setFiles(Object... files) {
        setFiles(List.of(files));
    }
}
