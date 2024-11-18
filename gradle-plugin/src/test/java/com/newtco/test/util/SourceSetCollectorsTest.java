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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gradle.api.file.FileTreeElement;
import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.newtco.test.util.SourceSetCollectors.SourceFileFilter;
import com.newtco.test.util.SourceSetCollectors.SourceFileTreeFilter;
import com.newtco.testlib.gradle.MockSourceSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A class for testing various functionalities related to source sets and file collections, primarily those defined in
 * the SourceSetCollectors utility.
 */
@SuppressWarnings("SameParameterValue")
public class SourceSetCollectorsTest {

    private static List<SourceSet> standardSourceSets;

    @BeforeAll
    public static void setup() {
        standardSourceSets = List.of(
            createSourceSet("main",
                getStdMainJavaFiles(true),
                getStdMainResourceFiles(true),
                getStdMainClassFiles(true),
                getStdMainResourceOutputs(true)),
            createSourceSet("test",
                getStdTestJavaFiles(true),
                getStdTestResourceFiles(true),
                getStdTestClassFiles(true),
                getStdTestResourceOutputs(true))
        );
    }

    private static Set<String> getStdMainJavaFiles(boolean relative) {
        var prefix = relative ? "" : "/home/proj/src/main/java/";

        // Java sources (6 files)
        return Set.of(
            prefix + "a/A1.java",
            prefix + "a/A2.java",
            prefix + "a/b/B1.java",
            prefix + "a/b/B2.java",
            prefix + "a/b/c/C1.java",
            prefix + "a/b/c/C2.java");
    }

    private static Set<String> getStdMainClassFiles(boolean relative) {
        var prefix = relative ? "" : "/home/proj/build/classes/java/main/";

        return Set.of(
            prefix + "a/A1.class",
            prefix + "a/A2.class",
            prefix + "a/A2$Inner1.class",
            prefix + "a/b/B1.class",
            prefix + "a/b/B2.class",
            prefix + "a/b/B2$Inner1.class",
            prefix + "a/b/B2$Inner1$Inner2.class",
            prefix + "a/b/c/C1.class",
            prefix + "a/b/c/C2$Inner1.class",
            prefix + "a/b/c/C2$Inner1$Inner2.class");
    }

    private static Set<String> getStdMainResourceFiles(boolean relative) {
        var prefix = relative ? "" : "/home/proj/src/main/resources/";

        return Set.of(
            prefix + "some random file.txt",
            prefix + "a/A1.properties",
            prefix + "a/b/B1.properties");
    }

    private static Set<String> getStdMainResourceOutputs(boolean relative) {
        var prefix = relative ? "" : "/home/proj/build/resources/main/";

        return Set.of(
            prefix + "some random file.txt",
            prefix + "a/A1.properties",
            prefix + "a/b/B1.properties");
    }

    private static Set<String> getStdTestJavaFiles(boolean relative) {
        var prefix = relative ? "" : "/home/proj/src/test/java/";

        return Set.of(
            prefix + "a/A2Test.java",
            prefix + "a/b/B1Test.java",
            prefix + "a/b/c/C1Test.java");
    }

    private static Set<String> getStdTestClassFiles(boolean relative) {
        var prefix = relative ? "" : "/home/proj/build/classes/java/test/";
        return Set.of(
            prefix + "a/A2Test.class",
            prefix + "a/b/B1Test.class",
            prefix + "a/b/c/C1Test.class",
            prefix + "a/b/c/C1Test$Inner.class");
    }

    private static Set<String> getStdTestResourceFiles(boolean relative) {
        var prefix = relative ? "" : "/home/proj/src/test/resources/";
        return Set.of(
            prefix + "some random file.txt",
            prefix + "a/b/B1Test.txt.properties");
    }

    private static Set<String> getStdTestResourceOutputs(boolean relative) {
        var prefix = relative ? "" : "/home/proj/build/resources/test/";
        return Set.of(
            prefix + "some random file.txt",
            prefix + "a/b/B1Test.txt.properties");
    }

    @SafeVarargs
    private static SourceSet createSourceSet(String name, Collection<String>... fileSets) {
        return MockSourceSet.create(
            name,
            "/home/proj",
            // Source & classes
            combineFileSets(fileSets).toArray()
        );
    }

    @SafeVarargs
    private static Set<String> combineFileSets(Collection<String>... fileSets) {
        var result = new HashSet<String>();
        for (var fileSet : fileSets) {
            result.addAll(fileSet);
        }
        return result;
    }

    private static String normalizePath(Object path) {
        return Objects.toString(path, "").replace('\\', '/');
    }

    private String pathToClassName(String file) {
        return file.replace('/', '.')
            .replace('$', '.')
            .replace(".java", "")
            .replace(".class", "");
    }

    private <K, V> Set<V> valueSets(Map<K, Set<V>> map) {
        var all = new HashSet<V>();
        for (var values : map.values()) {
            all.addAll(values);
        }
        return all;
    }

    /**
     * Helper to compare sets of what are expected to be files/paths
     */
    private <A, B> void assertEqualFileSet(Set<A> expected, Set<B> actual) {
        var normalizedExpected = expected.stream()
            .map(SourceSetCollectorsTest::normalizePath)
            .sorted()
            .collect(Collectors.joining("\n"));
        var normalizedActual = actual.stream()
            .map(SourceSetCollectorsTest::normalizePath)
            .sorted()
            .collect(Collectors.joining("\n"));
        assertEquals(normalizedExpected, normalizedActual, "Expected and actual file sets do not match.");
    }

    /**
     * Test that sourcesMatching correctly processes valid source sets, applies the filter and mapper functions, and
     * returns the expected result after applying the finisher function.
     */
    @Test
    @DisplayName("sourcesMatching should process valid sourceSets and return expected result")
    public void testSourcesMatchingWithValidInputs() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all .java files
        var filter = (SourceFileTreeFilter) (className, element) -> element.getFile().getName().endsWith(".java");

        // Define a mapper that maps className to its file path
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> element.getFile().getPath();

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, String>, Set<String>>) Map::keySet;

        // Act
        var result = SourceSetCollectors.sourcesMatching(stdSourceSets, filter, mapper, finisher);

        // Define the expected set of .java file paths
        var expected = combineFileSets(
            getStdMainJavaFiles(false),
            getStdTestJavaFiles(false)
        );

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that sourcesMatching returns an empty result when provided with empty source sets, ensuring that the method
     * can handle empty collections without throwing exceptions.
     */
    @Test
    @DisplayName("sourcesMatching should return empty result with empty sourceSets")
    public void testSourcesMatchingWithEmptySourceSets() {
        // Arrange
        var sourceSets = List.<SourceSet>of();

        // Define a filter (irrelevant since sourceSets are empty)
        var filter = (SourceFileTreeFilter) (className, element) -> true;

        // Define a mapper (irrelevant since sourceSets are empty)
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> className;

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, String>, Set<String>>) (map) -> new HashSet<>(map.values());

        // Act
        var result = SourceSetCollectors.sourcesMatching(sourceSets, filter, mapper, finisher);

        // Define the expected empty set
        var expected = Set.<String>of();

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that sourcesMatching behaves correctly when the filter accepts all files, verifying that the mapper and
     * finisher functions are applied to all elements.
     */
    @Test
    @DisplayName("sourcesMatching should process all files when filter accepts all")
    public void testSourcesMatchingWithFilterAcceptingAllFiles() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all files
        var filter = (SourceFileTreeFilter) (className, element) -> true;

        // Define a mapper that maps className to its uppercase form
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> className.toUpperCase();

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, String>, Set<String>>) Map::keySet;

        // Act
        var result = SourceSetCollectors.sourcesMatching(stdSourceSets, filter, mapper, finisher);

        // Define the expected set of all class names in uppercase
        var expected = combineFileSets(getStdMainJavaFiles(true), getStdTestJavaFiles(true))
            .stream()
            .map(this::pathToClassName)
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that sourcesMatching returns an empty result when the filter rejects all files, confirming that no elements
     * are processed by the mapper or finisher functions.
     */
    @Test
    @DisplayName("sourcesMatching should return empty result when filter rejects all files")
    public void testSourcesMatchingWithFilterRejectingAllFiles() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that rejects all files
        var filter = (SourceFileTreeFilter) (className, element) -> false;

        // Define a mapper (irrelevant since no files are accepted)
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> className;

        // Act
        var result = stdSourceSets.stream()
            .parallel()
            .collect(SourceSetCollectors.sourcesMatching(filter, mapper));

        // Define the expected empty set
        var expected = Map.<String, String>of();

        // Assert
        assertEquals(expected, result);
    }

    /**
     * Test that sourcesMatching correctly handles overlapping or duplicate class names in sourceSets, ensuring that the
     * mapper and finisher handle such cases as per the contract.
     */
    @Test
    @DisplayName("sourcesMatching should handle overlapping class names in sourceSets")
    public void testSourcesMatchingWithOverlappingClassNames() {
        // Arrange
        // Create two SourceSets with overlapping class names
        var mainSourceSet = createSourceSet("main",
            getStdMainJavaFiles(true),
            getStdMainClassFiles(true));

        var testSourceSet = createSourceSet("test",
            getStdTestJavaFiles(true),
            getStdTestClassFiles(true));

        var sourceSets = List.of(mainSourceSet, testSourceSet);

        // Define a filter that accepts all files
        var filter = (SourceFileTreeFilter) (className, element) -> true;

        // Act
        var result = SourceSetCollectors.sourceFilesMatching(sourceSets, filter);

        // Define the expected set of class file names
        var expected = combineFileSets(getStdMainJavaFiles(false), getStdTestJavaFiles(false))
            .stream()
            .map(SourceSetCollectorsTest::normalizePath)
            .collect(Collectors.toSet());

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that sourcesMatching correctly applies a custom mapper to extract only the class file names.
     */
    @Test
    @DisplayName("sourcesMatching should correctly apply a custom mapper to extract class file names")
    public void testSourcesMatchingWithCustomMapper() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all .java files
        var filter = (SourceFileTreeFilter) (className, element) -> element.getName().endsWith(".java");

        // Define a custom mapper that extracts only the class file name
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> element.getName();

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, String>, Set<String>>) Map::keySet;

        // Act
        var result = SourceSetCollectors.sourcesMatching(stdSourceSets, filter, mapper, finisher);

        // Define the expected set of class file names
        var expected = combineFileSets(getStdMainJavaFiles(false), getStdTestJavaFiles(false))
            .stream()
            .map(file -> new File(file).getName())
            .collect(Collectors.toSet());

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classFilesMatching correctly processes source sets, applies the filter, and returns the expected set of
     * class files.
     */
    @Test
    @DisplayName("classFilesMatching should filter and collect matching class files correctly")
    public void testClassFilesMatching() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all .class files related to B classes
        var filter = (SourceFileTreeFilter) (className, element) -> className.contains(".B");

        // Act
        var result = SourceSetCollectors.classFilesMatching(stdSourceSets, filter);

        // Define the expected set of B-related .class files
        var expected = combineFileSets(
            getStdMainClassFiles(false),
            getStdTestClassFiles(false)
        ).stream()
            .filter(file -> file.contains("/B"))
            .collect(Collectors.toSet());

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classesMatching correctly processes valid source sets, applies the filter and mapper functions, and
     * returns the expected result after applying the finisher function.
     */
    @Test
    @DisplayName("classesMatching should process valid sourceSets and return expected result")
    public void testClassesMatchingWithValidInputs() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all .class files
        var filter = (SourceFileTreeFilter) (className, element) -> true;

        // Define a mapper that maps className to its file path
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> element.getFile().getPath();

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, Set<String>>, Set<String>>) (map) -> {
            var set = new HashSet<String>();
            map.values().forEach(set::addAll);
            return set;
        };

        // Act
        var result = stdSourceSets.stream()
            .parallel()
            .collect(SourceSetCollectors.classesMatching(filter, mapper, finisher));

        // Define the expected set of .class file paths
        var expected = combineFileSets(
            getStdMainClassFiles(false),
            getStdTestClassFiles(false)
        );

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classesMatching returns an empty result when provided with empty source sets, ensuring that the method
     * can handle empty collections without throwing exceptions.
     */
    @Test
    @DisplayName("classesMatching should return empty result with empty sourceSets")
    public void testClassesMatchingWithEmptySourceSets() {
        // Arrange
        var sourceSets = List.<SourceSet>of();

        // Define a filter (irrelevant since sourceSets are empty)
        var filter = (SourceFileTreeFilter) (className, element) -> true;

        // Define a mapper (irrelevant since sourceSets are empty)
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> className;

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, Set<String>>, Set<String>>) this::valueSets;

        // Act
        var result = SourceSetCollectors.classesMatching(sourceSets, filter, mapper, finisher);

        // Define the expected empty set
        var expected = Set.<String>of();

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classesMatching behaves correctly when the filter accepts all files, verifying that the mapper and
     * finisher functions are applied to all elements.
     */
    @Test
    @DisplayName("classesMatching should process all files when filter accepts all")
    public void testClassesMatchingWithFilterAcceptingAllFiles() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all files
        var filter = (SourceFileTreeFilter) (className, element) -> true;

        // Define a mapper that maps className to its uppercase form
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> element.getPath();

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, Set<String>>, Set<String>>) (map) -> {
            var set = new HashSet<String>();
            map.values().forEach(set::addAll);
            return set;
        };

        // Act
        var result = SourceSetCollectors.classesMatching(stdSourceSets, filter, mapper, finisher);

        // Define the expected set of all class names in uppercase
        var expected = combineFileSets(getStdMainClassFiles(false), getStdTestClassFiles(false));

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classesMatching returns an empty result when the filter rejects all files, confirming that no elements
     * are processed by the mapper or finisher functions.
     */
    @Test
    @DisplayName("classesMatching should return empty result when filter rejects all files")
    public void testClassesMatchingWithFilterRejectingAllFiles() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that rejects all files
        var filter = (SourceFileTreeFilter) (className, element) -> false;

        // Define a mapper (irrelevant since no files are accepted)
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> className;

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, Set<String>>, Set<String>>) this::valueSets;

        // Act
        var result = SourceSetCollectors.classesMatching(stdSourceSets, filter, mapper, finisher);

        // Define the expected empty set
        var expected = Set.<String>of();

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classesMatching correctly handles overlapping or duplicate class names in sourceSets, ensuring that the
     * mapper and finisher handle such cases as per the contract.
     */
    @Test
    @DisplayName("classesMatching should handle overlapping class names in sourceSets")
    public void testClassesMatchingWithOverlappingClassNames() {
        // Arrange
        // Create two SourceSets with overlapping class names
        var mainSourceSet = createSourceSet("main",
            getStdMainJavaFiles(true),
            getStdMainClassFiles(true));

        var testSourceSet = createSourceSet("test",
            getStdTestJavaFiles(true),
            getStdTestClassFiles(true));

        var sourceSets = List.of(mainSourceSet, testSourceSet);

        // Define a filter that accepts class names containing ".A" or ".B"
        var filter = (SourceFileTreeFilter) (className, element) -> className.contains(".A") || className.contains(".B");

        // Define a mapper that maps className to the File object
        var mapper = (BiFunction<String, FileTreeElement, File>) (className, element) -> element.getFile();

        // Define a finisher that collects the File objects into a set
        var finisher = (Function<Map<String, Set<File>>, Set<File>>) this::valueSets;

        // Act
        var result = SourceSetCollectors.classesMatching(sourceSets, filter, mapper, finisher);

        // Define the expected set of class file names containing ".A" or ".B"
        var expected = combineFileSets(getStdMainClassFiles(false), getStdTestClassFiles(false))
            .stream()
            .filter(file -> file.contains("/A") || file.contains("/B"))
            .map(File::new)
            .collect(Collectors.toSet());

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Test that classesMatching correctly applies a custom mapper to extract only the class file names.
     */
    @Test
    @DisplayName("classesMatching should correctly apply a custom mapper to extract class file names")
    public void testClassesMatchingWithCustomMapper() {
        // Arrange
        var stdSourceSets = standardSourceSets;

        // Define a filter that accepts all .class files starting with "B" or "C"
        var filter = (SourceFileFilter) (className, file) -> file.getName().startsWith("B") || file.getName()
            .startsWith("C");

        // Define a custom mapper that extracts only the class file name
        var mapper = (BiFunction<String, FileTreeElement, String>) (className, element) -> element.getName();

        // Define a finisher that collects the map values into a set
        var finisher = (Function<Map<String, Set<String>>, Set<String>>) this::valueSets;

        // Act
        var result = SourceSetCollectors.classesMatching(stdSourceSets, filter, mapper, finisher);

        // Define the expected set of class file names starting with "B" or "C"
        var expected = combineFileSets(getStdMainClassFiles(false), getStdTestClassFiles(false))
            .stream()
            .map(file -> new File(file).getName())
            .filter(file -> file.startsWith("B") || file.startsWith("C"))
            .collect(Collectors.toSet());

        // Assert
        assertEqualFileSet(expected, result);
    }

    /**
     * Tests the default test() method of SourceFileFilter which delegates to the test(String, File) method.
     */
    @Test
    @DisplayName("SourceFileFilter.default test() should delegate correctly")
    public void testSourceFileFilter() {

        SourceFileFilter always = (className, file) -> true;
        SourceFileFilter never  = (className, file) -> false;

        assertFalse(always.and(never).test("a.B", new File("B")),
            "Should always return false");
        assertFalse(never.and(always).test("a.B", new File("B")),
            "Should always return false");

        assertTrue(always.or(never).test("a.B", new File("B")),
            "Should always return true");
        assertTrue(never.or(always).test("a.B", new File("B")),
            "Should always return true");
    }

    /**
     * Tests the default test() method of SourceFileFilter which delegates to the test(String, File) method.
     */
    @Test
    @DisplayName("SourceFileTreeFilter.default test() should delegate correctly")
    public void testSourceFileTreeFilter() {

        SourceFileTreeFilter always = (className, element) -> true;
        SourceFileTreeFilter never  = (className, element) -> false;

        assertFalse(always.and(never).test("a.B", null),
            "Should always return false");
        assertFalse(never.and(always).test("a.B", null),
            "Should always return false");

        assertTrue(always.or(never).test("a.B", null),
            "Should always return true");
        assertTrue(never.or(always).test("a.B", null),
            "Should always return true");
    }
}
