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
    id("base")
}

/**
 * Gradle task to add a license header to all source files
 */
abstract class AddLicenseHeadersTask : DefaultTask() {
    @Input
    @get:Option(option = "dry-run", description = "Show which files would be modified without making changes.")
    var dryRun: Boolean = false

    @InputFile
    val headerFile = project.rootProject.file("LICENSE_HEADER")

    @TaskAction
    fun addHeaders() {
        val headerText = headerFile.readText().replace("${'$'}{year}", java.time.Year.now().toString())

        project.fileTree(project.rootDir) {
            include("**/*.java", "**/*.kt", "**/*.gradle", "**/*.groovy", "**/*.kts")
            exclude(
                "/build/**",
                ".gradle/**",
                "META-INF/MANIFEST.MF"
            )
        }.forEach { file ->
            val content = file.readText()

            if (!content.startsWith(headerText.trim())) {
                if (dryRun) {
                    logger.lifecycle("Dry run: Would add header to ${file.path}")
                } else {
                    logger.lifecycle("Adding header to ${file.path}")
                    file.writeText(headerText + "\n" + content)
                }
            } else {
                logger.lifecycle("Header already exists in ${file.path}")
            }
        }
    }
}

/**
 * Gradle task to remove a license header from all source files
 */
abstract class RemoveLicenseHeadersTask : DefaultTask() {
    @Input
    @get:Option(option = "dry-run", description = "Show which files would be modified without making changes.")
    var dryRun: Boolean = false

    @InputFile
    val headerFile = project.file("LICENSE_HEADER")

    @TaskAction
    fun removeHeaders() {
        val headerText = headerFile.readText().trim()

        project.fileTree(project.rootDir) {
            include("**/*.java", "**/*.kt", "**/*.gradle", "**/*.groovy", "**/*.kts")
            exclude(
                "**/build/**",
                ".gradle/**",
                "META-INF/MANIFEST.MF"
            )
        }.forEach { file ->
            val content = file.readText()

            if (content.startsWith(headerText)) {
                if (dryRun) {
                    logger.lifecycle("Dry run: Would remove header from ${file.path}")
                } else {
                    logger.lifecycle("Removing header from ${file.path}")
                    val newContent = content.removePrefix(headerText).trimStart()
                    file.writeText(newContent)
                }
            } else {
                logger.lifecycle("Header not found in ${file.path}")
            }
        }
    }
}

tasks.register<AddLicenseHeadersTask>("addLicenseHeadersToSourceFiles")
tasks.register<RemoveLicenseHeadersTask>("removeLicenseHeadersFromSourceFiles")