package com.newtco.test.reports.api.coverage.model;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;

/**
 * Represents coverage data for a specific method. Extends the Coverage class with IMethodCoverage as the type.
 */
public class Method extends Coverage<IMethodCoverage> {
    private final String name;

    protected Method(IClassCoverage clazz, IMethodCoverage coverage) {
        super(coverage);
        this.name = Names.getMethodName(clazz, coverage);
    }

    public String getName() {
        return name;
    }
}
