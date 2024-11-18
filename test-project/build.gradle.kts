import com.newtco.test.reports.plugin.coverage.CoverageReportsExtension

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

plugins {
    id("java")
    id("jacoco")
    id("org.gradle.test-retry") version ("1.6.0")
    id("com.newtco.test.test-reports-plugin")
}


repositories {
    // Populated by the plugin project
    maven {
        name = "build"
        url = uri(file("../build/repo"))
    }
    mavenCentral()
}

group = project.properties["pluginGroup"] as String
version = project.properties["pluginVersion"] as String

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}


tasks.named("processReportTemplates").configure {
    setProperty("templatePackage", "com.newtco.test.report.templates")
}

tasks.withType<Test> {
    useJUnitPlatform()

    ignoreFailures = true

    testLogging {
        events("passed", "skipped", "failed")

        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        displayGranularity = -1
    }

    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }

    extensions.configure(com.newtco.test.reports.plugin.test.TestReportsExtension::class) {
        stackFilters {
            include(
                "org.opentest4j.**",
                "com.newtco.**"
            )
        }

        //

        json {
            enabled = true
            aggregateJsonReports = true
            includeSystemErrLog = true
            includeSystemOutLog = true
            outputPerTestCase = true

            testOutcomes("passed", "failed", "skipped")

        }

        summaryMarkdown {
            enabled = true
            aggregateReports = true
            includeSystemErrLog = true
            includeSystemOutLog = true
            outputPerTestCase = true
        }

        detailedMarkdown {
            enabled = true
            aggregateReports = true
            includeSystemErrLog = true
            includeSystemOutLog = true
            outputPerTestCase = true

            testOutcomes("passed", "failed", "skipped")
        }
    }
}

tasks.withType<JacocoReport> {
    reports {
        xml.required = true
        html.required = true
        csv.required = true
    }

    extensions.configure(CoverageReportsExtension::class) {
        json {
            enabled = true
            includeLines = true
            includeMethods = true
            includeClasses = true
            includeSources = true
            simplifiedCounters = false
        }

        summaryMarkdown {
            enabled = true
            badgeStyle = "flat-square"
            abbreviatePackages = true
        }

        detailedMarkdown {
            enabled = true
            badgeStyle = "flat-square"
            abbreviatePackages = false
        }
    }
}

tasks.register("detailedListConfigurations") {
    doLast {
        configurations.forEach { configuration ->
            println("Configuration name: ${configuration.name}")

            // Printing basic details
            println("  Can be resolved: ${configuration.isCanBeResolved}")
            println("  Can be consumed: ${configuration.isCanBeConsumed}")
            println("  Is visible: ${configuration.isVisible}")

            // Printing attributes
            configuration.attributes.keySet().forEach { attribute ->
                println("  Attribute: $attribute = ${configuration.attributes.getAttribute(attribute)}")
            }

            // Printing dependencies
            configuration.dependencies.forEach { dependency ->
                println("  Dependency: ${dependency.group}:${dependency.name}:${dependency.version}")
            }

            // Printing resolved dependencies (transitive including)
            if (configuration.isCanBeResolved) {
                try {
                    configuration.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                        println("  Resolved artifact: ${artifact.moduleVersion.id}")
                    }
                } catch (e: Exception) {
                    println("  Error resolving configuration: ${e.message}")
                }
            }

            // Only attempt to resolve dependencies if the configuration can be resolved
            if (configuration.isCanBeResolved) {
                try {
                    configuration.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                        println("  Resolved artifact: ${artifact.moduleVersion.id}")
                    }
                } catch (e: Exception) {
                    println("  Error resolving configuration: ${e.message}")
                }
            }


            // Printing artifacts
            configuration.artifacts.forEach { artifact ->
                println("  Artifact: ${artifact.file}")
            }
        }
    }
}
