package com.newtco.test.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitLinkTemplateTest {

    @DisplayName("UrlTemplate.createUrlBuilder(): Test valid inputs")
    @Test
    void shouldCreateLinkBuilderForValidInputs() {
        String                 template   = "{repository}/{commit}/{file}";
        GitLinkTemplate.GitLinkBuilder gitLinkBuilder = GitLinkTemplate.createLinkBuilder(template);

        assertNotNull(gitLinkBuilder, "UrlBuilder should not be null for valid inputs");

        String repository  = "myrepo";
        String commit      = "abc123";
        String file        = "src/main/java";
        String expectedUrl = repository + "/" + commit + "/" + file;

        assertEquals(expectedUrl, gitLinkBuilder.build(repository, commit, file),
                "UrlBuilder should generate correct URL for template " + template);
    }

    @DisplayName("UrlTemplate.createUrlBuilder(): Test invalid input with unmatched braces in template")
    @Test
    void shouldThrowExceptionForUnmatchedBracesInTemplate() {
        String template = "{repository/{commit}/{file}";

        assertThrows(IllegalArgumentException.class, () -> GitLinkTemplate.createLinkBuilder(template),
                "createUrlBuilder should throw IllegalArgumentException for unmatched braces in template");
    }

    @DisplayName("UrlTemplate.createUrlBuilder(): Test invalid input with unknown variable in template")
    @Test
    void shouldThrowExceptionForUnknownVariableInTemplate() {
        String template = "{someVariable}/{commit}/{file}";

        assertThrows(IllegalArgumentException.class, () -> GitLinkTemplate.createLinkBuilder(template),
                "createUrlBuilder should throw IllegalArgumentException for unknown variable in template");
    }
}