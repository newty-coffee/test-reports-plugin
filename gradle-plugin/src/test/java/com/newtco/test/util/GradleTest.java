package com.newtco.test.util;

import org.junit.jupiter.api.Test;

import com.newtco.testlib.gradle.MockProject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GradleTest {

    // Test to verify findProperty when property is found in project
    @Test
    void testFindPropertyInProject() {
        var    project       = MockProject.create();
        String propertyName  = "myProperty";
        String propertyValue = "propertyValue";
        project.setProperty(propertyName, propertyValue);

        String result = Gradle.findProperty(project, propertyName);
        assertEquals(propertyValue, result);
    }

    // Test to verify findProperty when property is not found in project but found in environment
    @Test
    void testFindPropertyInEnvironment() {
        var    project       = MockProject.create();
        String propertyName  = "envProperty";
        String propertyValue = "propertyValue";
        project.getProviders().setEnvironmentVariableForTest(propertyName, propertyValue);
        String result = Gradle.findProperty(project, propertyName);
        assertEquals(propertyValue, result);
    }

    @Test
    void testFindPropertyInGradle() {
        var    project       = MockProject.create();
        String propertyName  = "gradleProperty";
        String propertyValue = "propertyValue";
        project.getProviders().setGradlePropertyForTest(propertyName, propertyValue);
        String result = Gradle.findProperty(project, propertyName);
        assertEquals(propertyValue, result);
    }

    @Test
    void testFindPropertyInSystem() {
        var    project       = MockProject.create();
        String propertyName  = "systemProperty";
        String propertyValue = "propertyValue";
        project.getProviders().setSystemPropertyForTest(propertyName, propertyValue);
        String result = Gradle.findProperty(project, propertyName);
        assertEquals(propertyValue, result);
    }
    
    
    

    // Test to verify findProperty when property is neither in project nor in environment
    @Test
    void testFindPropertyNotFound() {
        var    project      = MockProject.create();
        String propertyName = "unknownProperty";
        String result       = Gradle.findProperty(project, propertyName);
        assertNull(result);
    }

    // Test to verify the property search order: Project > System > Gradle > Environment
    @Test
    void testFindPropertySearchOrder() {
        var    project       = MockProject.create();
        String propertyName  = "testProperty";
        String projectValue  = "projectValue";
        String systemValue   = "systemValue";
        String gradleValue   = "gradleValue";
        String envValue      = "envValue";

        project.setProperty(propertyName, projectValue);
        project.getProviders().setSystemPropertyForTest(propertyName, systemValue);
        project.getProviders().setGradlePropertyForTest(propertyName, gradleValue);
        project.getProviders().setEnvironmentVariableForTest(propertyName, envValue);

        // Should return project property first
        assertEquals(projectValue, Gradle.findProperty(project, propertyName));

        project.setProperty(propertyName, null);
        // Should return system property next
        assertEquals(systemValue, Gradle.findProperty(project, propertyName));

        project.getProviders().setSystemPropertyForTest(propertyName, null);
        // Should return gradle property next
        assertEquals(gradleValue, Gradle.findProperty(project, propertyName));

        project.getProviders().setGradlePropertyForTest(propertyName, null);
        // Should return environment property last
        assertEquals(envValue, Gradle.findProperty(project, propertyName));
    }
}