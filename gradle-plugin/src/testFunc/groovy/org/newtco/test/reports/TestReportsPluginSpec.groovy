package org.newtco.test.reports

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

    static class Params {
        String            projectName
        FileSystemFixture projectDir
        Path              buildFile
        Path              jacocoReportsDir
        Path              junitReportsDir
    }

    Params params

    def setup() {
        var dataVariables = getSpecificationContext().currentIteration.dataVariables

        params = new Params()
        params.projectName = "test-project-${dataVariables.gradleVersion}"
        params.projectDir = new FileSystemFixture(Path.of("build", params.projectName))
        params.buildFile = params.projectDir.file("build.gradle")
        params.jacocoReportsDir = params.projectDir.dir("build/reports/jacoco/test")
        params.junitReportsDir = params.projectDir.dir("build/test-results/test")

        // Copy the test project
        createTestProject(params.projectName, params.projectDir)
    }

    def cleanup() {
        if (null == getSpecificationContext().thrownException) {
//            cleanTestProject(params.projectDir.getCurrentPath())
        }
    }

    def createTestProject(String projectName, FileSystemFixture projectDir) {
        def projectPath = projectDir.getCurrentPath()

        println "Creating project structure ${projectPath}"

        projectDir.create {
            file("build.gradle").text = """
            plugins {
                id("java")
                id("jvm-test-suite")
                id("jacoco")
                id("org.newtco.test.test-reports-plugin")
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
            
            group = "org.newtco"
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
                dir("main/java/org/newtco") {
                    file("Main.java").text = """
                    package org.newtco;
                    
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

                dir("test/java/org/newtco") {
                    file("MainTests.java").text = """
                    package org.newtco;
                    
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
                    cleanTestProject(entry, false)
                }
            }
        }
        // Attempt to delete the directory or file itself
        Files.delete(path)
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

    // These tests are so slow, so test it all at once
    @Unroll
    def "can apply TestReportsPlugin with Gradle #gradleVersion"() {
        given: "A build file with the plugin applied"
        params.buildFile << """
        test {
            ignoreFailures = true
            
            additionalReports {
                gitLinkUrlTemplate = "https://newty.coffee/{file}?r={repository}&c={commit}"
                gitLinkCommit = "three"
            }            
                        
            extensions.configure(org.newtco.test.reports.plugin.test.TestReportsExtension) { 
                it.json {
                    enabled = true
                    
                    testOutcomes "passed", "failed", "skipped"
                }
            }
            
            extensions.getByType(org.newtco.test.reports.plugin.test.TestReportsExtension).configure {
                gitLinkRepository = "one/two"
            }
        }
        
        jacocoTestReport {
            additionalReports {
            }     
                   
            extensions.configure(org.newtco.test.reports.plugin.coverage.CoverageReportsExtension) {                
            }
        }
        """.stripIndent()
        when: "The build is run"
        def result = GradleRunner.create()
                                 .withGradleVersion(gradleVersion)
                                 .withPluginClasspath()
                                 .withProjectDir(params.projectDir.getCurrentPath().toFile())
                                 .withArguments("test", "jacocoTestReport", "--stacktrace", "--info")
                                 .forwardOutput()
                                 .build()

        then: "The plugin with[defaults] is applied successfully and templates compiled"
        result.output.contains('processReportTemplates')

        then: "The plugin with[defaults] generated the expected report files"
        verifyAll {
            Files.exists(params.jacocoReportsDir.resolve("jacocoTestReport.json"))
            Files.exists(params.jacocoReportsDir.resolve("jacocoTestReport.md"))
            Files.exists(params.jacocoReportsDir.resolve("jacocoTestReportSummary.md"))

            Files.exists(params.junitReportsDir.resolve("TEST.json"))
            Files.exists(params.junitReportsDir.resolve("TEST-detailed-org.newtco.MainTests.md"))
            Files.exists(params.junitReportsDir.resolve("TEST-summary-org.newtco.MainTests.md"))
        }

        and: "The JaCoCo JSON content is correct"
        def jacocoJson = parseJson(params.jacocoReportsDir.resolve("jacocoTestReport.json"))
        verifyAll {
            jacocoJson.report.name == params.projectName
            jacocoJson.report.counters.size() == 6
            jacocoJson.report.counters.method.covered == 4
            jacocoJson.report.counters.method.missed == 2
            jacocoJson.report.packages.size() == 1
            jacocoJson.report.packages[0].name == "org.newtco"
            jacocoJson.report.packages[0].classes.find { it.name == "Main" }.sourceFile == "Main.java"
            jacocoJson.report.packages[0].classes.find { it.name == "Main" }.counters.method.covered == 3
            jacocoJson.report.packages[0].classes.find { it.name == "Main" }.counters.method.missed == 1

        }

        and: "The JUnit JSON content is correct"
        def junitJson = parseJson(params.junitReportsDir.resolve("TEST.json"))
        verifyAll {
            junitJson.tests == 5
            junitJson.skipped == 1
            junitJson.failures == 3
            junitJson.testSuites.size() == 1
            junitJson.testSuites[0].name == "org.newtco.MainTests"
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