package com.newtco.test.reports.api.coverage.model;

import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * A representation of a class file's coverage information. This class extends the generic Coverage class for
 * IClassCoverage instances.
 */
public class ClassFile extends Coverage<IClassCoverage> {

    public ClassFile(IClassCoverage classCoverage) {
        super(classCoverage);
    }

    public String getName() {
        return Names.getClassName(coverage);
    }

    public String getQualifiedName() {
        return getPackageName() + "." + getName();
    }

    public String getAbbreviatedName() {
        return Names.getAbbreviatedPackageName(getQualifiedName());
    }

    public String getPackageName() {
        return Names.getPackageName(coverage.getPackageName());
    }

    public String getSourceFile() {
        return coverage.getSourceFileName();
    }

    public List<Method> getMethods() {
        return coverage.getMethods().stream()
            .map(this::newMethod)
            .toList();
    }

    private Method newMethod(IMethodCoverage method) {
        return new Method(this.coverage, method);
    }
}
