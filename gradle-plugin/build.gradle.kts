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
@file:Suppress("UnstableApiUsage")

plugins {
    id("java")
    id("jacoco")
    id("groovy")
    id("jvm-test-suite")
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    withJavadocJar()
    withSourcesJar()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            testType = TestSuiteType.UNIT_TEST

            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation(gradleTestKit())
                implementation("org.mockito:mockito-core:5.3.0")
                implementation("com.google.truth:truth:1.4.4")
            }
        }

        val testFunc by registering(JvmTestSuite::class) {
            testType = TestSuiteType.FUNCTIONAL_TEST

            useSpock()

            dependencies {
                implementation(project())
                implementation(gradleTestKit())
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(tasks.named("test"))
                    }
                }
            }
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
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

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
    ignoreFailures = false

    // Used by PluginIntegrationTest & test-project
    jvmArgs(
        "-Dplugin.maven.repo.dir=" + project.rootDir.parentFile.resolve("build/repo").absolutePath,
        "-Dplugin.build.dir=" + layout.buildDirectory.get().asFile.absolutePath,
        "-Dplugin.repository.rootDir=" + project.rootDir.parentFile.absolutePath
    )

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
}

gradlePlugin {
    website = "https://github.com/newty-coffee/test-reports-plugin"
    vcsUrl = "https://github.com/newty-coffee/test-reports-plugin"

    plugins {
        create("testReports") {
            id = "org.newtco.test.test-reports-plugin"
            implementationClass = "org.newtco.test.reports.plugin.TestReportsPlugin"
            displayName = "Test Reports Plugin"
            description = "JSON and Markdown reports for Jacoco and JUnit"
            tags = listOf("test", "reports", "jacoco", "junit", "markdown", "json")
        }
    }

    testSourceSets(
        sourceSets.named("test").get(),
        sourceSets.named("testFunc").get()
    )
}

tasks.named("publishPlugins") {
    enabled = !version.toString().endsWith("-SNAPSHOT")
}

tasks.withType<Jar> {
    archiveBaseName = "test-reports-plugin"
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    manifest {
        attributes(
            "Implementation-Title" to "Test Reports Gradle Plugin",
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to "newty.coffee",
        )
    }
}

val generatePluginVersion = tasks.register("generatePluginVersion") {
    val outputFile =
        layout.buildDirectory.file("generated/sources/plugin-version/org/newtco/test/reports/plugin/PluginVersion.java")

    outputs.file(outputFile)

    doFirst {
        outputFile.get().asFile.parentFile.mkdirs()
    }

    doLast {
        val plugin = gradlePlugin.plugins["testReports"]!!

        // Write the plugin properties to the plugin.properties file
        outputFile.get().asFile.writeText(
            """
            package org.newtco.test.reports.plugin;
            
            import java.util.Optional;
            import java.util.function.Supplier;
            
            import org.gradle.util.GradleVersion;
            
            public class PluginVersion {
                public static final String Name                 = "${plugin.name}";
                public static final String Id                   = "${plugin.id}";
                public static final String Group                = "${project.group}";
                public static final String Artifact             = "test-reports-plugin";
                public static final String Version              = "${project.version}";
                public static final String Timestamp            = "${System.currentTimeMillis()}";
                public static final String MinimumGradleVersion = "7.6.4";
            
                public static String coordinates() {
                    return "${project.group}:test-reports-plugin:${project.version}";
                }
            
                public static boolean snapshot() {
                    return ${project.version.toString().endsWith("-SNAPSHOT")};
                }
            
                public static void checkGradleVersion() {
                    if (GradleVersion.current().compareTo(GradleVersion.version(MinimumGradleVersion)) < 0) {
                        throw new UnsupportedOperationException("The test-reports-plugin requires Gradle " + MinimumGradleVersion + " or later");
                    }
                }
                
                public static boolean isGradleVersionAtLeast(String version) {
                    return GradleVersion.current().compareTo(GradleVersion.version(version)) >= 0;
                }

                public static void ifGradleVersionAtLeast(String version, Runnable runnable) {
                    if (isGradleVersionAtLeast(version)) {
                        runnable.run();
                    }
                }
                
                public static <T> Optional<T> ifGradleVersionAtLeast(String version, Supplier<T> supplier) {
                    if (isGradleVersionAtLeast(version)) {
                        return Optional.ofNullable(supplier.get());
                    }
                    return Optional.empty();
                }
            }
            """.trimIndent()
        )
    }
}

tasks.named("compileJava").configure {
    dependsOn(generatePluginVersion)
}

tasks.named("sourcesJar").configure {
    dependsOn(generatePluginVersion)
}

sourceSets {
    main {
        java {
            srcDirs(layout.buildDirectory.dir("generated/sources/plugin-version"))
        }
    }
}

tasks.named("processResources") {
    dependsOn(tasks.named("generatePluginVersion"))
}

fun isReleaseRun(): Boolean {
    return project.hasProperty("release")
}

if (!isReleaseRun()) {
    val buildRepoDir = project.rootDir.parentFile.resolve("build/repo").absolutePath
    println("Publishing plugin to local repo directory $buildRepoDir")

    // Shared by the test project for resolving build artifacts, which aren't published to the gradle plugin portal
    publishing {
        repositories {
            maven {
                name = "build"
                url = uri(buildRepoDir)
            }
        }
    }

    tasks.withType<Jar> {
        finalizedBy(tasks.named("publish"))
    }

    tasks.withType<Test> {
        dependsOn(tasks.named("publish"))
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            artifactId = "test-reports-plugin"

            pom {
                name = "Test Reports Plugin"
                description = "JSON and Markdown reports for JaCoCo and JUnit"
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

tasks.named("build", {
    dependsOn(tasks.named("test"), tasks.named("testFunc"))
})

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
