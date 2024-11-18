package com.newtco.test.reports.api.coverage.model;

import org.jacoco.core.analysis.IPackageCoverage;

import java.util.Comparator;
import java.util.List;

/**
 * Represents coverage data for a specific package. Extends the Coverage class with IPackageCoverage as the type.
 */
public class Package extends Coverage<IPackageCoverage> {
    public Package(IPackageCoverage packageCoverage) {
        super(packageCoverage);
    }

    public String getName() {
        return Names.getPackageName(coverage.getName());
    }

    public String getAbbreviatedName() {
        return Names.getAbbreviatedPackageName(getName().replace('/', '.'));
    }

    public List<ClassFile> getClasses() {
        return coverage.getClasses().stream()
                .map(ClassFile::new)
                .sorted(Comparator.comparing(ClassFile::getName))
                .toList();
    }

    public List<SourceFile> getSourceFiles() {
        return coverage.getSourceFiles().stream()
                .map(SourceFile::new)
                .sorted(Comparator.comparing(SourceFile::getName))
                .toList();
    }
}
