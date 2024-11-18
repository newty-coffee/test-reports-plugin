package com.newtco.test.reports.api.coverage.model;

import org.jacoco.core.analysis.ISourceFileCoverage;

/**
 * Represents coverage data for a specific source file. Extends the Coverage class with ISourceFileCoverage as the
 * type.
 */
public class SourceFile extends Coverage<ISourceFileCoverage> {

    public SourceFile(ISourceFileCoverage sourceFileCoverage) {
        super(sourceFileCoverage);
    }

    public String getName() {
        return coverage.getName();
    }
}
