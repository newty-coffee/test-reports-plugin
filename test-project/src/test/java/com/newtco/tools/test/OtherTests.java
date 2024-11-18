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

package com.newtco.tools.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OtherTests {

    @Test
    void shouldPassWhenTrue() {
        assertTrue(true, "This test should always pass");
    }

    @Test
    void shouldFailWhenFalse() {
        assertFalse(false, "This test should always fail");
    }

    @Test
    void shouldAddTwoNumbers() {
        int a = 5;
        int b = 10;
        assertEquals(15, a + b, "5 + 10 should equal 15");
    }
}
