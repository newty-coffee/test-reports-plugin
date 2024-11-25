package org.newtco.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextTest {

    /**
     * Test class for the Text utility class. This class primarily focuses on testing the isEmpty method.
     */

    @Test
    public void isEmpty_GivenNull_ReturnsTrue() {
        // arrange
        String testString = null;

        // act
        boolean result = Text.isEmpty(testString);

        // assert
        Assertions.assertTrue(result, "isEmpty should return true when argument is null");
    }

    @Test
    public void isEmpty_GivenEmptyString_ReturnsTrue() {
        // arrange
        String testString = "";

        // act
        boolean result = Text.isEmpty(testString);

        // assert
        Assertions.assertTrue(result, "isEmpty should return true when argument is an empty string");
    }

    @Test
    public void isEmpty_GivenNonEmptyString_ReturnsFalse() {
        // arrange
        String testString = "Test String";

        // act
        boolean result = Text.isEmpty(testString);

        // assert
        Assertions.assertFalse(result, "isEmpty should return false when argument is a non-empty string");
    }
}