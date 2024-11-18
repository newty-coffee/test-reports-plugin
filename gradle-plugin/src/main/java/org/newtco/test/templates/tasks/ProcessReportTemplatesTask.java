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

package org.newtco.test.templates.tasks;

import org.newtco.test.reports.api.coverage.CoverageTemplate;
import org.newtco.test.reports.api.test.TestTemplate;
import org.newtco.test.templates.TemplateCodeGenerator;
import org.newtco.test.templates.TemplateParser;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The ProcessReportTemplatesTask class is responsible for processing report template files and generating Java source
 * code based on the report-templates. The generated source files are placed in the specified output directory.
 */
public abstract class ProcessReportTemplatesTask extends SourceTask {
    public static final String TASK_NAME = "processReportTemplates";

    public ProcessReportTemplatesTask() {
        getTemplatePackage().convention("org.newtco.test.report.templates");

        // Always regenerate source code
        super.setOnlyIf("Do not cache", (unused) -> true);
    }

    /**
     * The directory property that will serve as the output directory for generated files. Configured by the plugin.
     *
     * @return the DirectoryProperty representing the output directory
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * The package name property which will be used for generating the Java source files. Can be overridden in a build
     * script, defaults org.newtco.test.report.templates
     *
     * @return the Property representing the package name.
     */
    @Input
    public abstract Property<String> getTemplatePackage();


    /**
     * Generates Java source files from template files located in the source directories. The method processes each
     * template, derives the corresponding package name, class name, parent class, and parses the template content to
     * generate the final source code. The generated source code is then written to the specified output directory.
     * <p>
     * Parent template classes are always one of the API template classes
     *
     * @throws IOException If an I/O error occurs during template processing or file writing
     */
    @TaskAction
    public void generate() throws IOException {
        var logger    = getLogger();
        var outputDir = getOutputDirectory().get().getAsFile().getAbsoluteFile();

        logger.lifecycle("ProcessTemplatesTask generating source files to output directory {}", outputDir);

        for (var templateFile : getSource().getFiles()) {
            logger.info("Processing template: {}", templateFile);

            // For each file in the source directories, the template can be one of:
            var packageName         = derivePackageNameFromTemplateFile(templateFile);
            var templateClassName   = deriveClassNameFromTemplateFile(templateFile);
            var templateParentClass = getParentTemplateClass(templateFile);
            var templateParts       = new TemplateParser().parse(Files.readString(templateFile.toPath()));

            var generatedSource = new TemplateCodeGenerator().generateTemplateClass(
                    packageName,
                    templateClassName,
                    templateParentClass,
                    templateParts);

            // Write the source to the target directory
            var outputFile = getOutputDirectory().dir(packageName.replace('.', '/'))
                    .get()
                    .file(templateClassName + ".java")
                    .getAsFile()
                    .getAbsoluteFile()
                    .toPath();
            Files.createDirectories(outputFile.getParent());
            Files.writeString(outputFile, generatedSource);

            logger.info("Generated template source file {}", outputFile);
        }
    }

    /**
     * Determines the appropriate parent template class for a given template file based on its name and directory.
     *
     * @param templateFile the file representing the template for which the parent class is to be derived
     * @return the Class object representing the appropriate parent template class for the given file
     * @throws IllegalArgumentException if the file does not have a supported name, or it is not located in a recognized
     *                                  directory
     */
    private Class<?> getParentTemplateClass(File templateFile) {
        var fileName   = templateFile.getName();
        var reportType = templateFile.getParentFile().getName();

        if ("coverage".equals(reportType)) {
            if (fileName.endsWith("MarkdownReport.jrt")) {
                return CoverageTemplate.class;
            }
        } else if ("tests".equals(reportType)) {
            if (fileName.endsWith("MarkdownReport.jrt")) {
                return TestTemplate.class;
            }
        }

        throw invalidTemplate(templateFile);
    }

    /**
     * Derives the package name from the given template file, based on the configured template package name and the type
     * of report based on the parent directory of the template file.
     *
     * @param templateFile the file representing the template from which the package name is to be derived
     * @return a string representing the derived package name
     */
    private String derivePackageNameFromTemplateFile(File templateFile) {
        var reportType = templateFile.getParentFile().getName().toLowerCase();

        return getTemplatePackage().get() + "." + reportType;
    }

    /**
     * Derives the class name from the given template file based on its naming convention. The name is converted to a
     * title case format followed by appending "Template".
     *
     * @param templateFile the file representing the template from which the class name is to be derived
     * @return a string representing the derived class name in title case format followed by "Template"
     * @throws IllegalArgumentException if the file does not conform to the expected template naming convention
     */
    private String deriveClassNameFromTemplateFile(File templateFile) {
        // All template files have the name form of SubType.(md|js).jrt
        var name = templateFile.getName();

        // Could just do a replacement, but then there's no validation
        if (name.endsWith("MarkdownReport.jrt")) {
            return titleCase(name.substring(0, name.indexOf("."))) + "Template";
        }

        throw invalidTemplate(templateFile);
    }

    private String titleCase(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private IllegalArgumentException invalidTemplate(File templateFile) {
        var message = "Unsupported template file \"" + templateFile.getAbsolutePath() + "\". " +
                "Only \"*MarkdownReport.jrt\" files are supported within \"coverage\" and \"tests\" directories.";

        return new IllegalArgumentException(message);
    }
}
