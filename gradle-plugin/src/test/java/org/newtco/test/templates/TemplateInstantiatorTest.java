package org.newtco.test.templates;

import org.newtco.test.reports.api.Template;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TemplateInstantiatorTest {

    private String pathOf(String path) {
        return Path.of(path).toString();
    }

    @Test
    public void testGetClassPathWithSingleEntry() {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        instantiator.addClasspath(pathOf("/example/path"));

        String classPath = instantiator.getClassPath();
        assertEquals(pathOf("/example/path"), classPath);
    }

    @Test
    public void testGetClassPathWithMultipleEntries() {


        TemplateInstantiator instantiator = new TemplateInstantiator();
        instantiator.addClasspath(
                pathOf("example/path2"),
                pathOf("/example/path2"));

        String classPath         = instantiator.getClassPath();
        String expectedClassPath = pathOf("example/path2") + File.pathSeparator + pathOf("/example/path2");
        assertEquals(expectedClassPath, classPath);
    }

    @Test
    public void testGetClassPathWithNoEntries() {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        String               classPath    = instantiator.getClassPath();
        assertEquals("", classPath);
    }

    String getTemplateInstantiatorTestClasspath() throws URISyntaxException, IOException {
        var classesDir = Paths.get(TemplateInstantiatorTest.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());

        var classPath = new ArrayList<String>();
        classPath.add(classesDir.toString());

        Files.walkFileTree(classesDir, new SimpleFileVisitor<>() {

            @Nonnull
            @Override
            public FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    classPath.add(file.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return String.join(File.pathSeparator, classPath);
    }


    @Test
    public void testCreateTemplateWithValidTemplateName() throws Exception {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        instantiator.setPackageName("org.newtco.test.templates");
        instantiator.addClasspath(getTemplateInstantiatorTestClasspath());

        Writer writerMock = mock(Writer.class);

        Template<?> template = instantiator.createTemplate("TemplateInstantiatorTest$TestTemplate", writerMock);

        assertNotNull(template);
        assertInstanceOf(TestTemplate.class, template);
        assertEquals(writerMock, template.getWriter());
    }

    @Test
    public void testCreateTemplateWithInvalidTemplateNameThrowsException() throws Exception {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        instantiator.setPackageName("org.newtco.test.templates");
        instantiator.addClasspath(getTemplateInstantiatorTestClasspath());

        assertThrows(ClassNotFoundException.class, () -> {
            instantiator.createTemplate("NonExistentTemplate");
        });
    }

    @Test
    public void testCreateTemplateWithClassNotAssignableThrowsException() throws Exception {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        instantiator.setPackageName("org.newtco.test.templates");
        instantiator.addClasspath(getTemplateInstantiatorTestClasspath());

        assertThrows(IllegalArgumentException.class, () -> {
            instantiator.createTemplate("TemplateInstantiatorTest$NonTemplateClass");
        });
    }

    @Test
    public void testSetPackageName() {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        String               packageName  = "org.newtco.test.templates";
        instantiator.setPackageName(packageName);

        assertEquals(packageName, instantiator.getPackageName());
    }

    @Test
    public void testSetPackageNameWithNull() {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        instantiator.setPackageName(null);

        assertNull(instantiator.getPackageName());
    }

    @Test
    public void testSetPackageNameWithEmptyString() {
        TemplateInstantiator instantiator = new TemplateInstantiator();
        String               packageName  = "";
        instantiator.setPackageName(packageName);

        assertEquals(packageName, instantiator.getPackageName());
    }

    public static class NonTemplateClass {
        public NonTemplateClass() {
        }
    }

    // Mock Template class to be used in tests
    public static class TestTemplate extends Template<TestTemplate> {
        public TestTemplate(Writer writer) {
            super(writer);
        }

        @Override
        protected TestTemplate self() {
            return this;
        }

        @Override
        public void render() throws Exception {

        }
    }

}