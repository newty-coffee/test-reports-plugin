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

package org.newtco.tools.test;

import org.newtco.test.test.Main;
import org.junit.jupiter.api.Test;
import org.opentest4j.FileInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Define MainTest class
public class MainTest {


    @Test
    public void test1_Test() {
        // Assert the result
        assertEquals("test1", new Main().test1());
    }


    @Test
    public void test2_Test() {
        // Define the expected result
        List<Object> expected = List.of("test2", 2, 2.0f, true, Map.of("test2", "test2"));

        // Assert the result
        assertEquals(expected, new Main().test2());
    }

    @Test
    public void test3_Test() {
        String expected = """
                test1
                test2
                `test3`
                unexpected line
                """;
        assertEquals(expected, new Main().test3());
    }

    @Test
    public void test_FrameworkException() {
        @SuppressWarnings({"NumericOverflow", "divzero"}) int x = 1 / 0;
    }

    @Test
    public void test_FileInfo() {
        var expectedData = "\n ! \" # $ % & ' ( ) * + , - . / 0 1 2 3 4 5 6 7 8 9 : ; < = > ? @ A B C D E F G H I J K L M N O P Q R S T U V W X Y Z [ \\ ] ^ _ ` a b c d e f g h i j k l m n o p q r s t u v w x y z { | } ~ \n";
        var actualData = new StringBuilder(expectedData).reverse().toString();

        FileInfo expected = new FileInfo("/path/expected", expectedData.getBytes());
        FileInfo actual   = new FileInfo("/path/actual", actualData.getBytes());
        assertEquals(expected, actual, "File contents match");
    }

    static class TestCallbackImpl implements Main.TestCallback<String> {
        String result;

        @Override
        public void callback(String value) {
            this.result = value;
        }
    }

    @Test
    void testCallbackWithNonNullValue() {
        TestCallbackImpl callbackImpl = new TestCallbackImpl();
        callbackImpl.callback("testValue");
        assertEquals("testValue", callbackImpl.result);
    }

    @Test
    void testCallbackWithNullValue() {
        TestCallbackImpl callbackImpl = new TestCallbackImpl();
        callbackImpl.callback(null);
        assertNull(callbackImpl.result);
    }

}