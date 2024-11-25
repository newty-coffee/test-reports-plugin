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

package org.newtco.test.reports.plugin;

import org.newtco.test.reports.plugin.coverage.CoverageReportsExtension;
import org.newtco.test.reports.plugin.coverage.CoverageReportGenerator;
import org.newtco.test.reports.plugin.test.TestReportGenerator;
import org.newtco.test.reports.plugin.test.TestReportsExtension;
import org.newtco.test.reports.plugin.test.TestSuiteCollector;
import org.newtco.test.reports.plugin.transform.PluginApiJarTransform;
import org.newtco.test.reports.plugin.transform.PluginJarType;
import org.newtco.test.templates.TemplateInstantiator;
import org.newtco.test.templates.tasks.ProcessReportTemplatesTask;
import org.newtco.test.util.GradleUtils.Actions;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.newtco.test.reports.plugin.transform.PluginJarType.PLUGIN_JAR_TYPE_ATTRIBUTE;
import static org.newtco.test.util.GradleUtils.Extensions.extensionOf;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE;


/**
 * Plugin to add additional report types to Jacoco and Junit
 */
public abstract class TestReportsPlugin implements Plugin<Project> {

    /**
     * Name of the source set added for report templates
     */
    public static final String REPORT_SOURCESET_NAME = "test-reports";
    /**
     * Name within the manifest of the plugin attributes added via the plugin's build.gradle.kts file. Needs to be kept
     * in sync with the value there.
     */
    public static final String PLUGIN_SECTION_NAME   = "plugins/test-reports-plugin/properties";

    private final Logger               logger;
    private final TemplateInstantiator templateInstantiator;

    @Inject
    public TestReportsPlugin() {
        this.logger               = Logging.getLogger(TestReportsPlugin.class);
        this.templateInstantiator = new TemplateInstantiator();
    }

    @Override
    public void apply(@NotNull Project project) {
        PluginVersion.checkGradleVersion();

        // Ensure jacoco plugin is also applied since we rely on it
        project.getPluginManager().apply("jacoco");

        configurePlugin(project);
    }

    private void configurePlugin(Project project) {
        configureTransform(project);
        configureTemplatesSourceSet(project);
        configureTestReports(project);
        configureJacocoReports(project);
        project.afterEvaluate(this::configureTemplateInstantiator);
    }

    /**
     * Registers the transform used to generate the plugin API jar
     */
    private void configureTransform(Project project) {
        var dependencies = project.getDependencies();

        // Register the custom attribute with gradle
        dependencies.attributesSchema(schema -> {
            schema.attribute(PLUGIN_JAR_TYPE_ATTRIBUTE);
        });

        // In order for Gradle to detect that it needs to convert from our full plugin JAR to our
        // API only jar, we need to set the default value for the attribute we use on JAR file
        // artifacts. This ensures that when we add the attribute with an API value, gradle knows
        // to convert it.
        dependencies.getArtifactTypes().named(ArtifactTypeDefinition.JAR_TYPE, jarArtifacts -> {
            jarArtifacts.getAttributes()
                    .attribute(PLUGIN_JAR_TYPE_ATTRIBUTE, PluginJarType.PLUGIN);
        });

        // Register the API transform
        dependencies.registerTransform(PluginApiJarTransform.class, transform -> {
            transform.getFrom()
                    .attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(PLUGIN_JAR_TYPE_ATTRIBUTE, PluginJarType.PLUGIN);

            transform.getTo()
                    .attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(PLUGIN_JAR_TYPE_ATTRIBUTE, PluginJarType.API);

            transform.parameters(params -> {
                params.getTimestamp().set(PluginVersion.Timestamp);
                params.getArtifactName().set(PluginVersion.Artifact);
            });
        });
    }

    /**
     * Configures the report-templates source set for the given project. This method sets up report template
     * directories, registers tasks for processing the report-templates, and configures dependencies for template APIs.
     *
     * @param project the project to which the report-templates source set should be added
     */
    private void configureTemplatesSourceSet(Project project) {
        // All templates exist in a single sourceSet
        var sourceSets = extensionOf(project, SourceSetContainer.class);

        var sourceSet = sourceSets.create(REPORT_SOURCESET_NAME);
        // Configure the report/templates directory
        var reportTemplatesSourceDirSet = project.getObjects().sourceDirectorySet("templates", "Report Templates");
        reportTemplatesSourceDirSet.getFilter().include("coverage/*.jrt", "tests/*.jrt");

        var templatesDir = "src/" + sourceSet.getName() + "/templates";
        reportTemplatesSourceDirSet.srcDir(templatesDir);

        sourceSet.getExtensions().add("templates", reportTemplatesSourceDirSet);
        sourceSet.getAllSource().source(reportTemplatesSourceDirSet);

        // Configure the generated java sources for complication. Report templates don't have main/test sources,
        // since they're only used by tests, but they're available to all test source sets
        var templatesJavaSourceDir = project.getLayout().getBuildDirectory()
                .dir("generated/sources/reportTemplates/java/");
        sourceSet.getJava().srcDir(templatesJavaSourceDir);

        // Add default templates
        addDefaultReportTemplates(project, project.file(templatesDir).toPath());

        // Configure the generator task which parses and creates Java source files from the templates
        project.getTasks().register(ProcessReportTemplatesTask.TASK_NAME, ProcessReportTemplatesTask.class, task -> {
            task.setGroup("other");
            task.setDescription("Generate Java sources from report templates");
            task.setSource(reportTemplatesSourceDirSet);
            task.getOutputDirectory().set(templatesJavaSourceDir);
        });

        // To generate constructors for the template classes, we need to ensure we can discover original
        // parameter names
        project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class)
                .configure(compileTask -> {
                    compileTask.dependsOn(ProcessReportTemplatesTask.TASK_NAME);
                    // The generator needs this
                    compileTask.getOptions().getCompilerArgs().add("-parameters");
                });

        // Link the custom sourceset compile task to the default testCompileJava task so it builds automatically
        project.getTasks()
                .named(sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).getCompileJavaTaskName(), JavaCompile.class)
                .configure(compile -> {
                    compile.dependsOn(sourceSet.getCompileJavaTaskName());
                });

        configureTemplatesApiDependencies(project, sourceSet);

        // We need the template instantiator to be able to load generated template classes...
        templateInstantiator.addClasspath(sourceSet.getOutput().getAsPath());
    }

    /**
     * Adds default report templates to the specified project and directory. This method checks if the templates already
     * exist in the report templates directory or if the plugin is in snapshot mode. If either condition is met, it
     * writes the default templates to the directory.
     *
     * @param project            the project to which default report templates should be added
     * @param reportTemplatesDir the directory where default report templates should be added
     */
    private void addDefaultReportTemplates(Project project, Path reportTemplatesDir) {
        logger.lifecycle("Adding default report templates to project {} dir {}",
                project.getDisplayName(),
                reportTemplatesDir.toAbsolutePath());

        var defaultTemplates = List.of(
                // coverage reports
                "coverage/SummaryMarkdownReport.jrt",
                "coverage/DetailedMarkdownReport.jrt",
                // unit test reports
                "tests/SummaryMarkdownReport.jrt",
                "tests/DetailedMarkdownReport.jrt"
        );

        var classLoader = getClass().getClassLoader();
        for (var defaultTemplate : defaultTemplates) {
            var templatePath = reportTemplatesDir.resolve(defaultTemplate);
            if (PluginVersion.snapshot() || !Files.exists(templatePath)) {
                try (var resource = classLoader.getResourceAsStream("report-templates" + "/" + defaultTemplate)) {
                    if (resource != null) {
                        Files.createDirectories(templatePath.getParent());
                        Files.write(reportTemplatesDir.resolve(defaultTemplate), resource.readAllBytes());
                    }
                } catch (IOException e) {
                    logger.error("Failed to add default template {}: {}", defaultTemplate, e.getMessage());

                    throw new UncheckedIOException("Failed to add default template " + defaultTemplate, e);
                }
            }
        }
    }

    /**
     * Configures the dependencies for the Template API from the report templates sourceSet. This method sets up the API
     * transform and necessary dependencies for both compiling and running the templates.
     *
     * @param sourceSet the source set to which the template dependencies should be configured
     * @param project   the project to which the source set belongs
     */
    private void configureTemplatesApiDependencies(Project project, SourceSet sourceSet) {
        var configurations = project.getConfigurations();

        configurations.named(sourceSet.getImplementationConfigurationName()).configure(configuration -> {
            configuration.defaultDependencies(dependencies -> {

                // Add the transformed plugin jar
                dependencies.add(
                        ((ModuleDependency) project.getDependencies().create(getPluginCoordinates(project)))
                                .attributes(attributes -> {
                                    attributes
                                            .attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                                            // Adding this attribute informs Gradle to run the transform on this dependency
                                            .attribute(PLUGIN_JAR_TYPE_ATTRIBUTE, PluginJarType.API);
                                })
                );

                var jacocoVersion = extensionOf(project, JacocoPluginExtension.class)
                        .getToolVersion();

                // Add JaCoCo dependencies
                dependencies.add(project.getDependencies()
                        .create("org.jacoco:org.jacoco.core:" + jacocoVersion));
                dependencies.add(project.getDependencies()
                        .create("org.jacoco:org.jacoco.report:" + jacocoVersion));
            });
        });
    }

    private Object getPluginCoordinates(Project project) {
        return PluginVersion.coordinates();
    }

    /**
     * Configures test reporting for the given project. Adds additional test report extensions, sets up a
     * TestGraphCollector to capture test outputs and results, and hooks into the test task lifecycle to generate
     * reports.
     *
     * @param project the project to which test reporting should be configured
     */
    private void configureTestReports(Project project) {
        project.getTasks().withType(Test.class).configureEach(test -> {
            var extension = test.getExtensions().create("additionalReports",
                    TestReportsExtension.class,
                    project,
                    test
            );

            AtomicReference<TestSuiteCollector> collector = new AtomicReference<>();

            test.doFirst(task -> {
                if (extension.getJson().getEnabled().get()
                        || extension.getSummaryMarkdown().getEnabled().get()
                        || extension.getSummaryMarkdown().getEnabled().get()) {

                    collector.set( new TestSuiteCollector(
                            extension.getStackFilters(),
                            extension.getGitLinkRepository(),
                            extension.getGitLinkCommit(),
                            extension.getGitLinkUrlTemplate()
                    ));

                    // Only collect test data if a report is enabled
                    test.addTestListener(collector.get());
                    test.addTestOutputListener(collector.get());
                }
            });

            test.doLast(task -> {
                if (extension.getJson().getEnabled().get()
                        || extension.getSummaryMarkdown().getEnabled().get()
                        || extension.getSummaryMarkdown().getEnabled().get()) {

                    generateTestReports(test, collector.get());
                }
            });
        });
    }

    /**
     * Assigns the configurable template package name from the ProcessReportTemplatesTask to the template instantiator,
     * which needs it to resolve generated templates.
     */
    private void configureTemplateInstantiator(Project project) {
        templateInstantiator.setPackageName(
                project.getTasks().named(ProcessReportTemplatesTask.TASK_NAME, ProcessReportTemplatesTask.class)
                        .flatMap(ProcessReportTemplatesTask::getTemplatePackage)
                        .get()
        );
    }

    /**
     * Configures Jacoco reports for the provided project. This method adds the CoverageReportExtension to all
     * JacocoReport tasks in the project and ensures that coverage reports are generated along with the JacocoReport
     * task.
     *
     * @param project the project for which Jacoco reports need to be configured
     */
    private void configureJacocoReports(Project project) {
        // Add the CoverageReportExtension to all JacocoReport tasks
        project.getTasks().withType(JacocoReport.class).configureEach(jacocoReport -> {
            jacocoReport.getExtensions().create(
                    "additionalReports",
                    CoverageReportsExtension.class,
                    project,
                    jacocoReport);

            // Generate reports along with the JacocoReport task
            jacocoReport.doLast(Actions.calling(this::generateCoverageReports));
        });
    }

    /**
     * Generates test reports for the given test task using a specified test graph collector.
     *
     * @param testTask  the test task for which the reports are generated
     * @param collector the collector used to gather test outputs and results
     */
    private void generateTestReports(Test testTask, TestSuiteCollector collector) {
        new TestReportGenerator(templateInstantiator, testTask, collector).generateTestReports();
    }

    /**
     * Generates coverage reports for the specified JacocoReport task.
     *
     * @param reportTask the JacocoReport task for which coverage reports need to be generated
     */
    private void generateCoverageReports(JacocoReport reportTask) {
        new CoverageReportGenerator(templateInstantiator, reportTask).generateCoverageReports();
    }
}



