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

package com.newtco.test.templates;

import com.newtco.test.reports.api.Template;
import com.newtco.test.util.Reflect;
import org.gradle.api.GradleException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The TemplateInstantiator class is responsible for dynamically loading classes that implement the Template interface
 * and creating instances of those classes. It maintains a classpath and class loader to manage the loading process.
 */
public class TemplateInstantiator {

    private final    Set<String> classpath;
    private volatile ClassLoader classLoader;
    private          String      packageName;

    public TemplateInstantiator() {
        this.classpath = new LinkedHashSet<>();
    }

    /**
     * Retrieves the package name that will be used to load templates.
     *
     * @return the currently set package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name to be used for loading templates.
     *
     * @param packageName the name of the package where templates are located.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Retrieves the current classpath as a single string, with each entry separated by the system's path separator.
     *
     * @return a string representing the current classpath.
     */
    public String getClassPath() {
        return String.join(File.pathSeparator, classpath);
    }

    /**
     * Adds one or more classpath entries to the current classpath.
     *
     * @param classpath one or more classpath entries to be added, each separated by the system's path separator
     */
    public void addClasspath(String... classpath) {
        for (var path : classpath) {
            this.classpath.addAll(List.of(path.split(File.pathSeparator)));
        }
    }

    /**
     * Creates a new instance of a template using the provided template name and parameters.
     *
     * @param templateName the name of the template class to be instantiated
     * @param parameters   the parameters to be passed to the constructor of the template class
     * @return an instance of the specified template
     * @throws ClassNotFoundException if the template class cannot be found
     */
    public Template<?> createTemplate(String templateName, Object... parameters) throws ClassNotFoundException {
        return (Template<?>) Reflect.newInstance(getTemplateClass(templateName), parameters);
    }

    private Class<?> getTemplateClass(String templateName) throws ClassNotFoundException {
        var canonicalName = packageName + "." + templateName;

        var clazz = getClassLoader().loadClass(canonicalName);
        if (Template.class.isAssignableFrom(clazz)) {
            return clazz;
        }

        throw new IllegalArgumentException("Class " + canonicalName + " is not assignable from " + Template.class.getName());
    }

    ClassLoader getClassLoader() {
        var local = classLoader;
        if (local == null) {
            var urls = classpath.stream()
                    .map(this::toClasspathURL)
                    .toArray(URL[]::new);

            synchronized (this) {
                local = classLoader;
                if (local == null) {
                    local = classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                }
            }
        }
        return local;
    }

    URL toClasspathURL(String path) {
        var file = new File(path);
        try {
            var name = file.getName();
            if (file.isDirectory()) {
                return new URL("file:" + file.getAbsolutePath() + "/");
            } else if (name.endsWith(".jar")) {
                return new URL("jar:file:" + file + "!/");
            } else {
                return new URL("file:" + file);
            }
        } catch (MalformedURLException e) {
            throw new GradleException("Failed to convert %s to URL".formatted(file.getAbsolutePath()), e);
        }
    }

}
