package org.newtco.test.util;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;

public class FilterSetTest {

    @Test
    public void testAsPredicate_includesMatch() {
        FilterSet filterSet = new FilterSet();
        filterSet.include("test*");

        Predicate<String> predicate = filterSet.asPredicate();

        assertThat(predicate.test("test123")).isTrue();
        assertThat(predicate.test("test_abc")).isTrue();
        assertThat(predicate.test("example123")).isFalse();
    }

    @Test
    public void testAsPredicate_excludesMatch() {
        FilterSet filterSet = new FilterSet();
        filterSet.exclude("test*");

        Predicate<String> predicate = filterSet.asPredicate();

        assertThat(predicate.test("test123")).isFalse();
        assertThat(predicate.test("test_abc")).isFalse();
        assertThat(predicate.test("example123")).isTrue();
    }

    @Test
    public void testAsPredicate_includeAndExcludeMatch() {
        FilterSet filterSet = new FilterSet();
        filterSet.include("include*");
        filterSet.exclude("exclude*");

        Predicate<String> predicate = filterSet.asPredicate();

        assertThat(predicate.test("include123")).isTrue();
        assertThat(predicate.test("exclude123")).isFalse();
        assertThat(predicate.test("example123")).isFalse();
    }

    @Test
    public void testAsPredicate_noIncludesOrExcludes() {
        FilterSet filterSet = new FilterSet();

        Predicate<String> predicate = filterSet.asPredicate();

        assertThat(predicate.test("anything")).isTrue();
        assertThat(predicate.test("goes")).isTrue();
    }

    @Test
    public void testAsPredicate_caseSensitive() {
        FilterSet filterSet = new FilterSet();
        filterSet.include("CaseSensitive");
        filterSet.setCaseSensitive(true);

        Predicate<String> predicate = filterSet.asPredicate();

        assertThat(predicate.test("casesensitive")).isFalse();
        assertThat(predicate.test("CaseSensitive")).isTrue();
    }

    @Test
    public void testAsPredicate_caseInsensitive() {
        FilterSet filterSet = new FilterSet();
        filterSet.include("CaseInsensitive");
        filterSet.setCaseSensitive(false);

        Predicate<String> predicate = filterSet.asPredicate();

        assertThat(predicate.test("caseinsensitive")).isTrue();
        assertThat(predicate.test("CaseInsensitive")).isTrue();
    }
}