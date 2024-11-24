package com.newtco.test.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

public class JdepsDependencyCollectorTest {

    @Test
    public void testGetDependencies_EmptyClasses() {
        JdepsDependencyCollector collector = new JdepsDependencyCollector();
        Set<String> result = collector.getDependencies(List.of(),
                JdepsDependencyCollector.classNameMapper(""));

        assertEquals(Set.of(), result);
    }

    @Test
    public void testGetDependencies_FoundDependencies() throws Exception {
        JdepsDependencyCollector collector = Mockito.spy(new JdepsDependencyCollector());

        File testFile = Files.createTempFile("TestClass", ".class").toFile();
        Path filePath = testFile.toPath();

        doReturn("  -> com.example.Dependency not found\n").when(collector).runJdeps(any());

        Set<String> result = collector.getDependencies(List.of(testFile),
                JdepsDependencyCollector.classNameMapper("com.example."));

        assertEquals(Set.of("com.example.Dependency"), result);
    }

    @Test
    public void testGetDependencies_NoDependencies() throws Exception {
        JdepsDependencyCollector collector = Mockito.spy(new JdepsDependencyCollector());

        File testFile = Files.createTempFile("TestClass", ".class").toFile();
        Path filePath = testFile.toPath();

        doReturn("").when(collector).runJdeps(any());

        Set<String> result = collector.getDependencies(List.of(testFile), JdepsDependencyCollector.classNameMapper(""));

        assertEquals(Set.of(), result);
    }

    @Test
    public void testGetDependencies_MultipleDependencies() throws Exception {
        JdepsDependencyCollector collector = Mockito.spy(new JdepsDependencyCollector());

        File testFile1 = Files.createTempFile("TestClass1", ".class").toFile();
        File testFile2 = Files.createTempFile("TestClass2", ".class").toFile();

        doReturn("  -> com.example.Dependency1 not found\n  -> com.example.Dependency2 not found\n").when(collector)
                .runJdeps(any());

        Set<String> result = collector.getDependencies(List.of(testFile1, testFile2),
                JdepsDependencyCollector.classNameMapper("com.example."));

        assertEquals(Set.of("com.example.Dependency1", "com.example.Dependency2"), result);
    }
}