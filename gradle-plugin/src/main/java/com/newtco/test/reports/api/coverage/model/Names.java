package com.newtco.test.reports.api.coverage.model;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.report.JavaNames;

/**
 * Provides utility methods for manipulating and retrieving names related to package, class, and method coverage data.
 */
public class Names {

    public static final JavaNames javaNames = new JavaNames();


    public static String getPackageName(String name) {
        return javaNames.getPackageName(name);
    }

    /**
     * Abbreviates a fully qualified name by retaining the last part fully and reducing all other parts to their
     * initials.
     *
     * @param name the fully qualified name to be abbreviated
     *
     * @return the abbreviated version of the name
     */
    public static String getAbbreviatedPackageName(String name) {
        var shortened = new StringBuilder();

        var parts = name.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            var part = parts[i];
            shortened.append(part.charAt(0));
            shortened.append('.');
        }
        shortened.append(parts[parts.length - 1]);
        return shortened.toString();
    }

    public static String getClassName(IClassCoverage node) {
        return javaNames.getClassName(
            node.getName(),
            node.getSignature(),
            node.getSuperName(),
            node.getInterfaceNames());
    }

    public static String getMethodName(IClassCoverage clazz, IMethodCoverage node) {
        return javaNames.getMethodName(
            clazz.getName(),
            node.getName(),
            node.getDesc(),
            node.getSignature()
        );
    }
}
