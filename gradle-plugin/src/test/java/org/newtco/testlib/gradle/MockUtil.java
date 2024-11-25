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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for handling common operations related to files and paths.
 */
public class MockUtil {

    public static File asFile(Object value) {
        if (value instanceof File file) {
            return file;
        } else if (value instanceof Path path) {
            return path.toFile();
        } else if (value instanceof String string) {
            return new File(string);
        }

        throw new UnsupportedOperationException("Unsupported Util.asFile() type: " + value.getClass());
    }

    public static Path asPath(Object value) {
        if (value instanceof File file) {
            return file.toPath();
        } else if (value instanceof Path path) {
            return path;
        } else if (value instanceof String string) {
            return Path.of(string);
        }

        throw new UnsupportedOperationException("Unsupported Util.asPath() type: " + value.getClass()
                + ". This is typically caused by passing collections to a accepting a vararg parameter that is eventually passed to this one.");
    }

    public static String extensionOf(Object file) {
        var name = asFile(file).getName();
        int dot  = name.lastIndexOf('.');
        if (dot == -1) {
            return "";
        } else {
            return name.substring(dot + 1);
        }
    }

    public static String normalizePath(Object path) {
        return asPath(path).toString().replace('\\', '/');
    }

    public static List<String> asFileTree(Object dir, Collection<?> files) {
        var dirPath = normalizePath(dir);

        return files.stream()
                .map(MockUtil::normalizePath)
                .<String>mapMulti((path, tree) -> {
                    int term = -1;
                    while ((term = path.indexOf('/', term + 1)) != -1) {
                        tree.accept(path.substring(0, term + 1));
                    }
                    tree.accept(path);
                })
                .distinct()
                .sorted()
                .map(path -> {
                    // Can't use path here because we require terminating / for dirs
                    return dirPath + "/" + path;
                })
                .toList();
    }

    public static Path asPath(Object first, Object... more) {
        var path = asPath(first);
        for (var part : more) {
            path = path.resolve(asPath(part));
        }
        return path;
    }

    public static Object[] javaFiles(Object... files) {
        return Stream.of(files)
                .filter(file -> "java".equals(extensionOf(file)))
                .toArray(Object[]::new);
    }

    public static Object[] classFiles(Object... files) {
        return Stream.of(files)
                .filter(file -> "class".equals(extensionOf(file)))
                .toArray(Object[]::new);
    }

    public static Object[] resourceFiles(Object... files) {
        return Stream.of(files)
                .filter(file -> !List.of("java", "class").contains(extensionOf(file)))
                .toArray(Object[]::new);
    }

    public static Object[] sourceFiles(Object... files) {
        return Stream.of(files)
                .filter(file -> !"class".equals(extensionOf(file)))
                .toArray(Object[]::new);
    }

    public static Object[] outputFiles(Object... files) {
        return Stream.of(files)
                .filter(file -> !"java".equals(extensionOf(file)))
                .toArray(Object[]::new);
    }

    public static Object[] combineFiles(Collection<?>... files) {
        var combined = new ArrayList<Object>();
        for (var fileSet : files) {
            combined.addAll(fileSet);
        }
        return combined.toArray();
    }

}
