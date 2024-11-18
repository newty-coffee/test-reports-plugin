package com.newtco.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlTemplateTest {

    @DisplayName("UrlTemplate.createUrlBuilder(): Test valid inputs")
    @Test
    void shouldCreateUrlBuilderForValidInputs() {
        String                 template   = "{repository}/{commit}/{path}";
        UrlTemplate.UrlBuilder urlBuilder = UrlTemplate.createUrlBuilder(template);

        assertNotNull(urlBuilder, "UrlBuilder should not be null for valid inputs");

        String repository  = "myrepo";
        String commit      = "abc123";
        String path        = "src/main/java";
        String expectedUrl = repository + "/" + commit + "/" + path;

        assertEquals(expectedUrl, urlBuilder.build(repository, commit, path),
            "UrlBuilder should generate correct URL for template " + template);
    }

    @DisplayName("UrlTemplate.createUrlBuilder(): Test invalid input with unmatched braces in template")
    @Test
    void shouldThrowExceptionForUnmatchedBracesInTemplate() {
        String template = "{repository/{commit}/{path}";

        assertThrows(IllegalArgumentException.class, () -> UrlTemplate.createUrlBuilder(template),
            "createUrlBuilder should throw IllegalArgumentException for unmatched braces in template");
    }

    @DisplayName("UrlTemplate.createUrlBuilder(): Test invalid input with unknown variable in template")
    @Test
    void shouldThrowExceptionForUnknownVariableInTemplate() {
        String template = "{someVariable}/{commit}/{path}";

        assertThrows(IllegalArgumentException.class, () -> UrlTemplate.createUrlBuilder(template),
            "createUrlBuilder should throw IllegalArgumentException for unknown variable in template");
    }
}