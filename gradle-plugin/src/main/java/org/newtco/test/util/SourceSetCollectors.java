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

package org.newtco.test.util;

import org.gradle.api.file.FileTreeElement;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

/**
 * A utility class for constructing Stream Collectors that accumulate elements from a {@code SourceSet} based on
 * specified filters and mappers.
 */
public class SourceSetCollectors {
    /**
     * A method that processes source sets by filtering and mapping their elements, and then applying a finishing
     * function to produce the final result.
     *
     * @param sourceSets the collection of source sets to be processed
     * @param filter     the filter criteria determining which files are included
     * @param mapper     the mapping function to transform the filtered elements
     * @param finisher   the function to produce the final result from the accumulated elements
     * @param <R>        the type of the mapped result
     * @param <U>        the type of the final result
     * @return the processed result produced by applying the finisher function to the mapped elements
     */
    public static <R, U> U sourcesMatching(
            Collection<SourceSet> sourceSets,
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper,
            Function<Map<R, String>, U> finisher
    ) {
        return sourceSets.stream()
                .collect(sourcesMatching(filter, mapper, finisher));
    }

    /**
     * Filters and collects source files from the given collection of source sets based on the provided filter.
     *
     * @param sourceSets the collection of source sets to be processed
     * @param filter     the filter criteria determining which files are included
     * @return a set of files that match the filter criteria
     */
    public static Set<File> sourceFilesMatching(
            Collection<SourceSet> sourceSets,
            SourceFileTreeFilter filter
    ) {
        return sourceSets.stream()
                .collect(sourceFilesMatching(filter));
    }

    /**
     * Constructs a {@code Collector} that accumulates elements from a {@code SourceSet} based on the specified filter
     * and mapper, and then applies a finishing function to produce the final result.
     *
     * @param filter   A {@code BiPredicate} that tests each element in the {@code SourceSet} for inclusion. It receives
     *                 a qualified class name and a {@code FileTreeElement}.
     * @param mapper   A {@code BiFunction} that maps the filtered elements to desired values. It receives a qualified
     *                 class name and a {@code FileTreeElement}.
     * @param finisher A {@code Function} that applies a finishing transformation to the accumulated {@code Map} of
     *                 results.
     * @param <R>      The mapped type of the values in the accumulating {@code Map}.
     * @param <U>      The final result type of the {@code Collector}.
     * @return A {@code Collector} that accumulates source set elements into a {@code Map<R, String>} and then applies
     * the specified finishing transformation to produce the final result of type {@code U}.
     */
    public static <R, U> Collector<SourceSet, Map<R, String>, U> sourcesMatching(
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper,
            Function<Map<R, String>, U> finisher
    ) {
        return Collector.of(
                HashMap::new,
                makeSourcesAccumulator(filter, mapper),
                SourceSetCollectors::sourcesCombiner,
                finisher,
                Characteristics.UNORDERED);
    }

    /**
     * Constructs a {@code Collector} that accumulates elements from a {@code SourceSet} based on the specified filter
     * and mapper, and then produces a {@code Map<R, String>} as the final result.
     *
     * @param filter A {@code BiPredicate} that tests each element in the {@code SourceSet} for inclusion. It receives a
     *               qualified class name and a {@code FileTreeElement}.
     * @param mapper A {@code BiFunction} that maps the filtered elements to desired values. It receives a qualified
     *               class name and a {@code FileTreeElement}.
     * @param <R>    The mapped type of the values in the accumulating {@code Map}.
     * @return A {@code Collector} that accumulates source set elements into a {@code Map<R, String>}.
     */
    public static <R> Collector<SourceSet, Map<R, String>, Map<R, String>> sourcesMatching(
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper
    ) {
        return sourcesMatching(
                filter,
                mapper,
                Function.identity()
        );
    }

    /**
     * Constructs a {@code Collector} that accumulates file elements from a {@code SourceSet} that match the specified
     * filter.
     *
     * @param filter A {@code BiPredicate} that tests each file in the {@code SourceSet} for inclusion. It receives a
     *               qualified class name and a {@code File} object.
     * @return A {@code Collector} that accumulates matching files from the {@code SourceSet} into a {@code Set<File>}.
     */
    public static Collector<SourceSet, Map<File, String>, Set<File>> sourceFilesMatching(
            SourceFileTreeFilter filter
    ) {
        return sourcesMatching(
                filter,
                SourceSetCollectors::fileMapper,
                Map::keySet
        );
    }

    /**
     * Constructs a {@code Collector} that accumulates elements from a {@code SourceSet} based on the specified filter
     * and mapper, then inverts the map into a collection of source class names to mapped value. The inverter assumes
     * that SourceSets do not contain overlapping source files.
     *
     * @param filter A {@code BiPredicate} that tests each element in the {@code SourceSet} for inclusion. It receives a
     *               qualified class name and a {@code FileTreeElement}.
     * @param mapper A {@code BiFunction} that maps the filtered elements to desired values. It receives a qualified
     *               class name and a {@code FileTreeElement}.
     * @param <R>    The mapped type of the values in the accumulating {@code Map}.
     * @return A {@code Collector} that accumulates source set elements into a {@code Map<R, String>} and then applies
     * an inverting finisher to produce a {@code Map<String, R>} of class names to {@code R}.
     */
    public static <R> Collector<SourceSet, Map<R, String>, Map<String, R>> sourceClassesMatching(
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper
    ) {
        return sourcesMatching(
                filter,
                mapper,
                SourceSetCollectors::mapInverter);
    }

    /**
     * Processes a collection of SourceSet objects to find matching classes based on the given filter, then maps and
     * aggregates the results using the provided functions.
     *
     * @param sourceSets the collection of SourceSet objects to be processed
     * @param filter     the filter used to select the classes of interest
     * @param mapper     a function that takes a class name and a file tree element, and returns a mapped result
     * @param finisher   a function that takes a map of class names to mapped results and returns a finished result
     * @return the finished result, as produced by the finisher function
     */
    public static <R, U> U classesMatching(
            Collection<SourceSet> sourceSets,
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper,
            Function<Map<String, Set<R>>, U> finisher
    ) {
        return sourceSets.stream()
                .collect(classesMatching(filter, mapper, finisher));
    }

    /**
     * Finds and returns a set of class files from the given collection of source sets that match the specified filter.
     *
     * @param sourceSets A collection of SourceSet objects representing the source sets to be searched.
     * @param filter     A filter to apply on the source files to determine which files should be included as class
     *                   files.
     * @return A set of Files representing the class files that match the provided filter.
     */
    public static Set<File> classFilesMatching(
            Collection<SourceSet> sourceSets,
            SourceFileTreeFilter filter
    ) {
        return sourceSets.stream()
                .collect(classFilesMatching(filter));
    }

    /**
     * Returns a {@code Collector} returning a {@code Map<String, File>} of output class names to output class files
     * matching source files accepted by the specified {@code filter}.
     *
     * @param filter {@code BiPredicate} receiving a Java source file and qualified class name returning true to include
     *               the matching class file in the collected output.
     */
    public static <R, U> Collector<SourceSet, Map<String, Set<R>>, U> classesMatching(
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper,
            Function<Map<String, Set<R>>, U> finisher
    ) {
        return Collector.of(
                HashMap::new,
                makeClassesAccumulator(filter, mapper),
                SourceSetCollectors::classesCombiner,
                finisher,
                Characteristics.UNORDERED
        );
    }

    /**
     * Constructs a {@code Collector} that accumulates class files from a {@code SourceSet} based on the specified
     * filter.
     *
     * @param filter A {@code BiPredicate} that tests each class file in the {@code SourceSet} for inclusion. It
     *               receives a qualified class name and a {@code FileTreeElement}.
     * @return A {@code Collector} that accumulates the matching class files from the {@code SourceSet} into a
     * {@code Set<File>}.
     */
    public static Collector<SourceSet, Map<String, Set<File>>, Set<File>> classFilesMatching(
            SourceFileTreeFilter filter
    ) {
        return classesMatching(
                filter,
                SourceSetCollectors::fileMapper,
                SourceSetCollectors::valuesMerger);
    }

    /**
     * Collects and processes class files from a given {@link SourceSet}. This method first filters and accumulates
     * qualified class names, and then visits each class file in the output to apply the given consumer and mapper
     * functions if the class name is present in the accumulated set.
     *
     * @param <R>       The type of the mapped result.
     * @param sourceSet The {@link SourceSet} containing the source files to be processed.
     * @param consumer  A {@link BiConsumer} that processes each qualified class name and its corresponding mapped
     *                  result.
     * @param filter    A {@link SourceFileTreeFilter} that filters source files based on their names and details.
     * @param mapper    A {@link BiFunction} that maps a qualified class name and its corresponding
     *                  {@link FileTreeElement} to a result of type {@code R}.
     */
    private static <R> void accumulateClasses(
            SourceSet sourceSet,
            BiConsumer<String, R> consumer,
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper
    ) {
        var classNames = new HashSet<String>();
        accumulateSources(
                sourceSet,
                makeClassNameConsumer(classNames),
                filter,
                SourceSetCollectors::classNameMapper);
        if (classNames.isEmpty()) {
            return;
        }

        sourceSet.getOutput().getClassesDirs().getAsFileTree().visit(details -> {
            if (!details.isDirectory()) {
                var path = details.getRelativePath().getPathString();
                if (!path.endsWith(".class")) {
                    return;
                }

                var qualifiedName = qualifiedName(path);
                if (classNames.contains(qualifiedName)) {
                    consumer.accept(qualifiedName, mapper.apply(qualifiedName, details));
                }
            }
        });
    }

    /**
     * Accumulates elements from a given {@code SourceSet} based on the specified filter and mapper, and applies the
     * given consumer to each accumulated element.
     *
     * @param <R>       The type of the mapped result.
     * @param sourceSet The {@code SourceSet} containing the source files to be processed.
     * @param consumer  A {@code BiConsumer} that processes each qualified class name and its corresponding mapped
     *                  result.
     * @param filter    A {@code SourceFileTreeFilter} that filters source files based on their names and details.
     * @param mapper    A {@code BiFunction} that maps a qualified class name and its corresponding
     *                  {@code FileTreeElement} to a result of type {@code R}.
     */
    private static <R> void accumulateSources(
            SourceSet sourceSet,
            BiConsumer<R, String> consumer,
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper
    ) {
        sourceSet.getAllJava().getAsFileTree().visit(details -> {
            if (!details.isDirectory()) {
                var qualifiedName = qualifiedName(details.getRelativePath().getPathString());
                if (filter.test(qualifiedName, details)) {
                    consumer.accept(mapper.apply(qualifiedName, details), qualifiedName);
                }
            }
        });
    }

    private static <R> Map<String, Set<R>> classesCombiner(Map<String, Set<R>> out1, Map<String, Set<R>> out2) {
        for (var entry : out2.entrySet()) {
            out1.computeIfAbsent(entry.getKey(), SourceSetCollectors::newSet).addAll(entry.getValue());
        }
        return out1;
    }

    private static File fileMapper(String qualifiedName, FileTreeElement element) {
        return element.getFile();
    }

    private static String classNameMapper(String qualifiedName, FileTreeElement element) {
        return qualifiedName;
    }

    private static <R> BiConsumer<String, R> makeClassNameConsumer(Set<String> classNames) {
        return (String className, R unused) -> classNames.add(className);
    }

    private static <R> BiConsumer<Map<String, Set<R>>, SourceSet> makeClassesAccumulator(
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper
    ) {
        return (Map<String, Set<R>> outputs, SourceSet sourceSet) -> accumulateClasses(sourceSet,
                makeMapPutAndAddConsumer(outputs), filter, mapper);
    }

    private static <T, R> BiConsumer<T, R> makeMapPutAndAddConsumer(Map<T, Set<R>> map) {
        return (T key, R value) -> map.computeIfAbsent(key, SourceSetCollectors::newSet).add(value);
    }

    private static <R> BiConsumer<Map<R, String>, SourceSet> makeSourcesAccumulator(
            SourceFileTreeFilter filter,
            BiFunction<String, FileTreeElement, R> mapper
    ) {
        return (Map<R, String> outputs, SourceSet sourceSet) -> accumulateSources(sourceSet, outputs::put, filter,
                mapper);
    }

    private static <K, V> Set<V> newSet(K unused) {
        return new HashSet<>();
    }

    /**
     * Converts a relative .java or .class file path into a qualified outer class name.
     *
     * @param path Class file path with '/' separators
     */
    private static String qualifiedName(String path) {
        // if nested class
        int term = path.lastIndexOf('/');
        if (term != -1) {
            term = path.indexOf('$', term);
            if (term != -1) {
                return path.substring(0, term).replace('/', '.');
            }
        }

        // Normal class - Strip file name
        term = path.lastIndexOf('.');
        if (term != -1) {
            return path.substring(0, term).replace('/', '.');
        }

        return path.replace('/', '.');
    }

    private static <R> Map<R, String> sourcesCombiner(Map<R, String> out1, Map<R, String> out2) {
        out1.putAll(out2);
        return out1;
    }

    /**
     * Combines all sets of a map of sets
     */
    private static <K, V> Set<V> valuesMerger(Map<K, Set<V>> map) {
        var values = new HashSet<V>();
        for (var value : map.values()) {
            values.addAll(value);
        }
        return values;
    }

    private static <R> Map<String, R> mapInverter(Map<R, String> map) {
        var inverted = new HashMap<String, R>();
        for (var entry : map.entrySet()) {
            R previous = inverted.put(entry.getValue(), entry.getKey());
            if (previous != null) {
                throw new IllegalStateException("Duplicate key %s (attempted merging values %s and %s)".formatted(
                        entry.getValue(), previous, entry.getKey()));
            }
        }
        return inverted;
    }

    /**
     * Represents a filter for source files, providing methods for combining filters using logical AND, OR operations.
     * This functional interface facilitates filtering based on both class name and file parameters.
     */
    @FunctionalInterface
    public interface SourceFileTreeFilter {
        boolean test(String className, FileTreeElement element);

        default SourceFileTreeFilter and(SourceFileTreeFilter other) {
            return (className, element) -> test(className, element) && other.test(className, element);
        }

        default SourceFileTreeFilter or(SourceFileTreeFilter other) {
            return (className, element) -> test(className, element) || other.test(className, element);
        }
    }

    /**
     * Defines a filter interface for source files, providing methods to evaluate files based on their class name and
     * file object. It also provides default methods for combining filters using logical AND, OR operations.
     */
    public interface SourceFileFilter extends SourceFileTreeFilter {
        boolean test(String className, File file);

        default boolean test(String className, FileTreeElement element) {
            return test(className, element.getFile());
        }

        default SourceFileFilter and(SourceFileFilter other) {
            return (className, file) -> test(className, file) && other.test(className, file);
        }

        default SourceFileFilter or(SourceFileFilter other) {
            return (className, file) -> test(className, file) || other.test(className, file);
        }
    }

    /**
     * A functional interface extending SourceFileFilter to provide a filter mechanism specifically targeting class
     * names. This interface abstracts the need to handle file objects directly, focusing instead on filtering by class
     * name.
     */
    @FunctionalInterface
    public interface SourceClassNameFilter extends SourceFileFilter {
        boolean test(String className);

        default boolean test(String className, File file) {
            return test(className);
        }
    }
}


