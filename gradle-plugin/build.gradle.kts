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
    id("groovy") // for tests
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = project.properties["pluginGroup"] as String
version = project.properties["pluginVersion"] as String

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    modularity.inferModulePath = true

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    withJavadocJar()
    withSourcesJar()
}

sourceSets {
    test {
        groovy {
            srcDirs("src/test/groovy")
        }
    }
}


dependencies {
    gradleApi()

    implementation("org.jacoco:org.jacoco.core:${JacocoPlugin.DEFAULT_JACOCO_VERSION}")
    implementation("org.jacoco:org.jacoco.report:${JacocoPlugin.DEFAULT_JACOCO_VERSION}")
    implementation("com.github.javaparser:javaparser-core:3.26.2")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:5.3.0")
    // For assertions
    testImplementation("com.google.truth:truth:1.4.4")

    // For Spock tests
    testImplementation("org.spockframework:spock-core:2.2-groovy-3.0") {
        exclude(group = "org.codehaus.groovy")
    }


    testImplementation(gradleTestKit())
    testImplementation(localGroovy())
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Needed so reflection can build subclass constructors
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all"))
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Javadoc> {
    isFailOnError = false

    options
        .encoding("UTF-8")
        .source("17")
        .optionFiles(project.file("javadoc.options"))

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
}

tasks.withType<JacocoReport> {
    reports {
        xml.required = true
        html.required = true
        csv.required = true
    }

    afterEvaluate({
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/Test.class")
                }
            })
        )
    })
}

gradlePlugin {
    website = "https://github.com/newty-coffee/test-reports-plugin"
    vcsUrl = "https://github.com/newty-coffee/test-reports-plugin"

    plugins {
        create("testReports") {
            id = "com.newtco.test.test-reports-plugin"
            implementationClass = "com.newtco.test.reports.plugin.TestReportsPlugin"
            displayName = "Test Reports Plugin"
            description = "JSON and Markdown reports for JUnit and Jacoco"
            tags = listOf("test", "reports", "jacoco", "markdown", "json")
        }
    }
}

tasks.named("publishPlugins") {
    enabled = !version.toString().endsWith("-SNAPSHOT")
}

tasks.withType<Jar> {
    archiveBaseName = "test-reports-plugin"
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    manifest {
        val plugin = gradlePlugin.plugins["testReports"]!!

        attributes(
            "Implementation-Title" to "Test Reports Gradle Plugin",
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to "newty.coffee",
        )

        attributes(
            "plugins/test-reports-plugin/properties",
            "plugin-name" to plugin.name,
            "plugin-id" to plugin.id,
            "plugin-group" to project.group,
            "plugin-artifact" to archiveBaseName,
            "plugin-version" to project.version,
            "plugin-timestamp" to System.currentTimeMillis(),
        )
    }
}


if (project.version.toString().endsWith("-SNAPSHOT")) {
    // Shared by the test project for resolving build artifacts, which aren't published to the gradle plugin portal
    publishing {
        repositories {
            maven {
                name = "build"
                url = uri("../build/repo")
            }
        }
    }

    tasks.withType<Jar> {
        finalizedBy(tasks.named("publish"))
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            artifactId = "test-reports-plugin"

            pom {
                name = "Test Reports Plugin"
                description = "JSON and Markdown reports for JUnit and Jacoco"
                url = "https://github.com/newty-coffee/test-reports-plugin"
                inceptionYear = "2023"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                        distribution = "repo"
                    }
                }
            }
        }
    }
}

tasks.register("listConfigurations") {
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

tasks.register("listPublications") {
    doLast {
        println("Configured Publications:\n")
        publishing.publications.forEach { publication ->
            if (publication is MavenPublication) {
                println("- MavenPublication")
                println("  Name: ${publication.name}")
                println("  Group: ${publication.groupId}")
                println("  ArtifactId: ${publication.artifactId}")
                println("  Version: ${publication.version}")

                println("  Artifacts:")
                publication.artifacts.forEach { artifact ->
                    println("    - ${artifact.file.name} (type: ${artifact.classifier ?: "main"})")
                }

                val pom = publication.pom
                println("  POM Information:")
                println("     Packaging:      ${pom.packaging}")
                println("     Name:           ${pom.name.getOrNull()}")
                println("     Description:    ${pom.description.getOrNull()}")
                println("     Url:            ${pom.url.getOrNull()}")
                println("     Inception Year: ${pom.inceptionYear.getOrNull()}")
                pom.properties.get().forEach { (key, value) ->
                    println("     Property:       $key = $value")
                }
                println("  Published Coordinates: ${publication.groupId}:${publication.artifactId}:${publication.version}")
            } else {
                println("- Publication name: ${publication.name}")
                println("  Type: ${publication::class.simpleName}")
                println("  (Non-MavenPublication; details not printed)")
            }
            println("")
        }
    }
}
