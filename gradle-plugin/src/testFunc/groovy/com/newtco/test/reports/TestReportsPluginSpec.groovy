package com.newtco.test.reports

import groovy.json.JsonSlurper
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.io.FileSystemFixture

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class TestReportsPluginSpec extends Specification {

    // Set in gradle-plugin/gradle.build.kts Test task configuration
    @Shared
    Path repositoryDir = Path.of(System.getProperty("plugin.repository.rootDir"))

    @Shared
    Path mavenRepoDir = Path.of(System.getProperty("plugin.maven.repo.dir"))

    @Shared
    AtomicInteger uniqueId = new AtomicInteger(0)

    String projectName = "test-project-${LocalDateTime.now().format("MMdd")}-${(uniqueId.incrementAndGet() + "").padLeft(4, "0")}"

    FileSystemFixture projectDir = new FileSystemFixture(Path.of("build", projectName))

    Path buildFile = projectDir.file("build.gradle")

    Path jacocoReportsDir = projectDir.dir("build/reports/jacoco/test")
    Path junitReportsDir  = projectDir.dir("build/test-results/test")

    def setup() {
        // Copy the test project
        createTestProject(projectDir)
    }

    def cleanup() {
        //TODO: delete temp dir if test successful
    }

    def createTestProject(FileSystemFixture projectDir) {
        def projectPath = projectDir.getCurrentPath()

        if (Files.exists(projectPath)) {
            println("Cleaning test project directory ${projectPath}")
            cleanTestProject(projectDir.getCurrentPath())
        }

        println "Creating project structure ${projectPath}"

        projectDir.create {
            file("build.gradle").text = """
            plugins {
                id("java")
                id("jvm-test-suite")
                id("jacoco")
                id("com.newtco.test.test-reports-plugin")
            }
            
            repositories {
                // Populated by the plugin project
                maven {
                    name = "build"
                    // gradle-plugin publishes to this file repo during non-CI builds
                    url = uri(file("${mavenRepoDir.toString().replace('\\', '/')}"))
                }
                
                mavenCentral()            
            }
            
            group = "com.newtco"
            version = "1.0.0-SNAPSHOT"
            
            testing {
                suites {
                    test {
                        useJUnitJupiter()
                        
                        dependencies {
                            implementation("org.opentest4j:opentest4j:1.3.0")
                        }
                    }
                }
            }
            """.stripIndent()


            file("settings.gradle").text = """
            rootProject.name = "${projectName}"
            """.stripIndent()

            dir("src") {
                dir("main/java/com/newtco") {
                    file("Main.java").text = """
                    package com.newtco;
                    
                    public class Main {
                        public String func1() {
                            return "func1 called";
                        }
                        
                        public String func2() {
                            return new WithCoverage().echo("func2\\ncalled");
                        }
                        
                        public String func3() {
                            // No coverage
                            return "func3 not called";
                        }
                        
                        private static class WithCoverage {
                            public String echo(String value) {
                                return value;
                            }
                        }
                        
                        private static class NoCoverage {
                            public String echo(String value) {
                                return value;
                            }
                        }                        
                    }
                    """.stripIndent()
                }

                dir("test/java/com/newtco") {
                    file("MainTests.java").text = """
                    package com.newtco;
                    
                    import org.junit.jupiter.api.*;                    
                    import org.opentest4j.*;
                    
                    public class MainTests {
                        @Test
                        public void test_passed() {
                            Assertions.assertEquals("func1 called", new Main().func1());
                        }        
                        
                        @Test
                        public void test_assertionFailure() {
                            Assertions.assertEquals("func2 called", new Main().func2());
                        }
                        
                        @Test
                        @Disabled
                        public void test_skipped() {
                            // This test is skipped and will not be executed
                        }
                                                
                        @Test
                        public void test_frameworkFailure() {
                            @SuppressWarnings({"NumericOverflow", "divzero"}) int x = 1 / 0;
                        }
                        
                        @Test
                        public void test_fileFailure() {
                            var expected = "\\n!\\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\n";
                            var actual = new StringBuilder(expected).reverse().toString();

                            Assertions.assertEquals(
                                new FileInfo("/path/to/expected.file", expected.getBytes()),
                                new FileInfo("/path/to/actual.file", actual.getBytes())
                            );
                        }
                    }
                    """.stripIndent()
                }
            }
        }
    }

    def cleanTestProject(Path path, boolean isRoot = true) {
        if (!Files.exists(path)) {
            return // Directory doesn't exist, nothing to do
        }
        if (Files.isDirectory(path)) {
            // List all entries in the directory and delete them recursively
            Files.newDirectoryStream(path).withCloseable { entries ->
                entries.each { entry ->
                    if (entry.fileName.toString() != '.gradle') {
                        cleanTestProject(entry, false)
                    }
                }
            }
        }
        // Attempt to delete the directory or file itself
        if (!isRoot) {
            Files.delete(path)
        }
    }

    /**
     * Parses a JSON file and returns the content as a Map.
     *
     * @param jsonFilePath Path to the JSON file.
     * @return Parsed JSON content as a Map.
     */
    def parseJson(Path jsonFilePath) {
        new JsonSlurper().parse(jsonFilePath.toFile()) as Map
    }

    /**
     * Asserts that a specific field in the JSON matches the expected value.
     *
     * @param jsonContent Parsed JSON content.
     * @param fieldPath Dot-separated path to the field.
     * @param expectedValue Expected value or closure to evaluate the value.
     */
    def assertJsonField(Map jsonContent, String fieldPath, def expectedValue) {
        def actualValue = fieldPath.tokenize('.').inject(jsonContent) { current, key ->
            current[key]
        }
        if (expectedValue instanceof Closure) {
            assert expectedValue.call(actualValue)
        } else {
            assert actualValue == expectedValue
        }
    }


    // These tests are so slow, so test it all at once
    @Unroll
    def "can apply TestReportsPlugin with Gradle #gradleVersion"() {
        given: "A build file with the plugin applied"
        buildFile << """
        test {
            ignoreFailures = true
            
            additionalReports {
                gitLinkUrlTemplate = "https://newty.coffee/{file}?r={repository}&c={commit}"
                gitLinkCommit = "three"
            }            
                        
            extensions.configure(com.newtco.test.reports.plugin.test.TestReportsExtension) { 
                it.json {
                    enabled = true
                    
                    testOutcomes "passed", "failed", "skipped"
                }
            }
            
            extensions.getByType(com.newtco.test.reports.plugin.test.TestReportsExtension).configure {
                gitLinkRepository = "one/two"
            }
        }
        
        jacocoTestReport {
            additionalReports {
            }     
                   
            extensions.configure(com.newtco.test.reports.plugin.coverage.CoverageReportsExtension) {                
            }
        }
        """.stripIndent()
        when: "The build is run"
        def result = GradleRunner.create()
                                 .withGradleVersion(gradleVersion)
                                 .withPluginClasspath()
                                 .withProjectDir(projectDir.getCurrentPath().toFile())
                                 .withArguments("test", "jacocoTestReport", "--stacktrace", "--info")
                                 .forwardOutput()
                                 .build()

        then: "The plugin with[defaults] is applied successfully and templates compiled"
        result.output.contains('processReportTemplates')

        then: "The plugin with[defaults] generated the expected report files"
        verifyAll {
            Files.exists(jacocoReportsDir.resolve("jacocoTestReport.json"))
            Files.exists(jacocoReportsDir.resolve("jacocoTestReport.md"))
            Files.exists(jacocoReportsDir.resolve("jacocoTestReportSummary.md"))

            Files.exists(junitReportsDir.resolve("TEST.json"))
            Files.exists(junitReportsDir.resolve("TEST-detailed-com.newtco.MainTests.md"))
            Files.exists(junitReportsDir.resolve("TEST-summary-com.newtco.MainTests.md"))
        }

        and: "The JaCoCo JSON content is correct"
        def jacocoJson = parseJson(jacocoReportsDir.resolve("jacocoTestReport.json"))
        verifyAll {
            jacocoJson.report.name == projectName
            jacocoJson.report.counters.size() == 6
            jacocoJson.report.counters.method.covered == 4
            jacocoJson.report.counters.method.missed == 2
            jacocoJson.report.packages.size() == 1
            jacocoJson.report.packages[0].name == "com.newtco"
            jacocoJson.report.packages[0].classes[0].name == "Main"
            jacocoJson.report.packages[0].classes[0].sourceFile == "Main.java"

        }

        and: "The JUnit JSON content is correct"
        def junitJson = parseJson(junitReportsDir.resolve("TEST.json"))
        verifyAll {
            junitJson.tests == 5
            junitJson.skipped == 1
            junitJson.failures == 3
            junitJson.testSuites.size() == 1
            junitJson.testSuites[0].name == "com.newtco.MainTests"
            junitJson.testSuites[0].testCases.find { it.name == "test_assertionFailure()" }.status == "failed"
            junitJson.testSuites[0].testCases.find { it.name == "test_fileFailure()" }.status == "failed"
            junitJson.testSuites[0].testCases.find { it.name == "test_frameworkFailure()" }.status == "failed"
            junitJson.testSuites[0].testCases.find { it.name == "test_passed()" }.status == "passed"
            junitJson.testSuites[0].testCases.find { it.name == "test_skipped()" }.status == "skipped"
        }

        where:
        gradleVersion << ["7.6.4", "8.8", "8.9", "8.10.2", "8.11"]
    }
}