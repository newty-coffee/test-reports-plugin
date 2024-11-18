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

package com.newtco.tools.test;// Import necessary libraries

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.newtco.test.test.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Define MainTest class
public class MainTest {

    /*
    MainTest class for testing Main class. Contains test methods for Main.test1, test2, and test3 methods.
     */

    @Test
    public void test1_Test() {

        // Create objects of ByteArrayOutputStream and PrintStream
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        PrintStream standardOut = System.out;

        // Change the print statements to print in the outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));

        // Instantiate Main class and call test1 method
        Main main = new Main();
        main.test1();

        // Reset the standard out stream
        System.setOut(standardOut);

        // Assert the result
        assertEquals("test1\n", outputStreamCaptor.toString());
    }

    @Test
    public void test2_Test() {

        // Create objects of ByteArrayOutputStream and PrintStream
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        PrintStream standardOut = System.out;

        // Change the print statements to print in the outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));

        // Instantiate Main class and call test2 method
        Main main = new Main();
        main.test2();

        // Reset the standard out stream
        System.setOut(standardOut);

        // Assert the result
        assertEquals("test2\n", outputStreamCaptor.toString());
    }

    @Test
    public void test3_True_Test() {

        // Create objects of ByteArrayOutputStream and PrintStream
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        PrintStream standardOut = System.out;

        // Change the print statements to print in the outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));

        // Instantiate Main class and call test3 method with true flag
        Main main = new Main();
        main.test3(true);

        // Reset the standard out stream
        System.setOut(standardOut);

        // Assert the result
        assertEquals("test3:true\n", outputStreamCaptor.toString());
    }

    @Test
    public void test3_False_Test() {

        // Create objects of ByteArrayOutputStream and PrintStream
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        PrintStream standardOut = System.out;

        // Change the print statements to print in the outputStreamCaptor
        System.setOut(new PrintStream(outputStreamCaptor));

        // Instantiate Main class and call test3 method with false flag
        Main main = new Main();
        main.test3(false);

        // Reset the standard out stream
        System.setOut(standardOut);

        // Assert the result
        assertEquals("test3:false\n", outputStreamCaptor.toString());
    }
}