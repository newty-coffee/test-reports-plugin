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

package com.newtco.test.util;

import org.gradle.api.reflect.ObjectInstantiationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Reflection utility class.
 */
public class ReflectTest {

    /**
     * Tests successful instantiation using an exact match constructor with Integer parameter.
     */
    @Test
    public void testNewInstanceExactMatchWithInteger() {
        Integer value = 42;
        SampleClassWithIntegerConstructor instance = Reflect.newInstance(SampleClassWithIntegerConstructor.class,
                value);
        assertNotNull(instance);
        assertEquals(value, instance.getValue(), "The Integer value should be set correctly.");
    }

    /**
     * Tests successful instantiation when passing an Integer to a constructor expecting an int.
     */
    @Test
    public void testNewInstancePrimitiveConstructorWithWrapperParameter() {
        Integer value = 42;
        SampleClassWithIntConstructor instance = Reflect.newInstance(SampleClassWithIntConstructor.class,
                value);

        assertNotNull(instance);
        assertEquals(value.intValue(),
                instance.getValue(),
                "The int value should be set correctly from Integer parameter.");
    }

    /**
     * Tests successful instantiation when passing an int to a constructor expecting an Integer.
     */
    @Test
    public void testNewInstanceWrapperConstructorWithPrimitiveParameter() {
        int value = 42;
        SampleClassWithIntegerConstructor instance = Reflect.newInstance(SampleClassWithIntegerConstructor.class,
                value);
        assertNotNull(instance);
        assertEquals(Integer.valueOf(value),
                instance.getValue(),
                "The Integer value should be set correctly from int parameter.");
    }

    /**
     * Tests instantiation when both primitive and wrapper constructors are available. Verifies that the correct
     * constructor is called based on the parameter.
     */
    @Test
    public void testNewInstanceWithBothConstructors() {
        // Passing Integer value
        Integer integerValue = 42;
        SampleClassWithBothConstructors instance1 = Reflect.newInstance(SampleClassWithBothConstructors.class,
                integerValue);
        assertNotNull(instance1);
        assertEquals("Integer",
                instance1.getConstructorUsed(),
                "Should use the Integer constructor when passing Integer.");

        // Passing int value
        int intValue = 42;
        SampleClassWithBothConstructors instance2 = Reflect.newInstance(SampleClassWithBothConstructors.class,
                intValue);
        assertNotNull(instance2);
        assertEquals("Integer",
                instance2.getConstructorUsed(),
                "Should prefer the Integer constructor when both are available.");
    }

    /**
     * Tests successful instantiation with multiple parameters, mixing primitives and wrappers.
     */
    @Test
    public void testNewInstanceWithMultipleParameters() {
        Integer intValue    = 42;
        String  stringValue = "Test";
        SampleClassWithMultipleParameters instance = Reflect.newInstance(SampleClassWithMultipleParameters.class,
                intValue,
                stringValue);
        assertNotNull(instance);
        assertEquals(42, instance.getIntValue(), "The int value should be set correctly from Integer parameter.");
        assertEquals("Test", instance.getStringValue(), "The String value should be set correctly.");
    }

    /**
     * Tests that an exception is thrown when no matching constructor is found.
     */
    @Test
    public void testNewInstanceNoMatchingConstructor() {
        assertThrows(ObjectInstantiationException.class, () -> {
            Reflect.newInstance(SampleClassWithNoMatchingConstructor.class, 42); // No constructor with int
        }, "Should throw ObjectInstantiationException when no matching constructor is found.");
    }


    /**
     * Tests successful instantiation when a null parameter is passed.
     */
    @Test
    public void testNewInstanceNullParameter() {
        SampleClassWithIntegerConstructor instance = Reflect.newInstance(SampleClassWithIntegerConstructor.class, (Integer) null);
        assertNotNull(instance);
        assertNull(instance.getValue(), "The value should be null when null parameter is passed.");
    }

    /**
     * Tests that an ObjectInstantiationException is thrown when incompatible parameter types are used.
     */
    @Test
    public void testNewInstanceIncompatibleTypes() {
        assertThrows(ObjectInstantiationException.class, () -> {
            Reflect.newInstance(SampleClassWithIntConstructor.class, "NotAnInt");
        }, "Should throw ObjectInstantiationException when incompatible parameter types are used.");
    }

    /**
     * Tests instantiation with a constructor that accepts a double, passing a Double object.
     */
    @Test
    public void testNewInstanceDoubleConstructorWithWrapperParameter() {
        Double value = 3.14;
        SampleClassWithDoubleConstructor instance = Reflect.newInstance(SampleClassWithDoubleConstructor.class,
                value);
        assertNotNull(instance);
        assertEquals(value, instance.getValue(), "The double value should be set correctly from Double parameter.");
    }

    /**
     * Tests instantiation when passing a Double to a constructor expecting a double.
     */
    @Test
    public void testNewInstancePrimitiveDoubleConstructorWithWrapperParameter() {
        Double value = 3.14;
        SampleClassWithDoubleConstructor instance = Reflect.newInstance(SampleClassWithDoubleConstructor.class,
                value);
        assertNotNull(instance);
        assertEquals(value.doubleValue(),
                instance.getValue(),
                "The double value should be set correctly from Double parameter.");
    }

    /**
     * Tests instantiation when passing a double to a constructor expecting a Double.
     */
    @Test
    public void testNewInstanceWrapperDoubleConstructorWithPrimitiveParameter() {
        double value = 3.14;
        SampleClassWithDoubleWrapperConstructor instance = Reflect.newInstance(
                SampleClassWithDoubleWrapperConstructor.class,
                value);
        assertNotNull(instance);
        assertEquals(Double.valueOf(value),
                instance.getValue(),
                "The Double value should be set correctly from double parameter.");
    }

    /**
     * Test class with a constructor that accepts an Integer.
     */
    private static class SampleClassWithIntegerConstructor {
        private final Integer value;

        public SampleClassWithIntegerConstructor(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    /**
     * Test class with a constructor that accepts an int.
     */
    private static class SampleClassWithIntConstructor {
        private final int value;

        public SampleClassWithIntConstructor(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Test class with both int and Integer constructors.
     */
    private static class SampleClassWithBothConstructors {
        private final String constructorUsed;

        public SampleClassWithBothConstructors(int value) {
            this.constructorUsed = "int";
        }

        public SampleClassWithBothConstructors(Integer value) {
            this.constructorUsed = "Integer";
        }

        public String getConstructorUsed() {
            return constructorUsed;
        }
    }

    /**
     * Test class with a constructor that accepts multiple parameters.
     */
    private static class SampleClassWithMultipleParameters {
        private final int    intValue;
        private final String stringValue;

        public SampleClassWithMultipleParameters(int intValue, String stringValue) {
            this.intValue    = intValue;
            this.stringValue = stringValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public String getStringValue() {
            return stringValue;
        }
    }

    /**
     * Test class with no matching constructor.
     */
    private static class SampleClassWithNoMatchingConstructor {
        public SampleClassWithNoMatchingConstructor(double value) {
            // Constructor accepting a double
        }
    }

    /**
     * Test class with a constructor that accepts a double.
     */
    private static class SampleClassWithDoubleConstructor {
        private final double value;

        public SampleClassWithDoubleConstructor(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    /**
     * Test class with a constructor that accepts a Double.
     */
    private static class SampleClassWithDoubleWrapperConstructor {
        private final Double value;

        public SampleClassWithDoubleWrapperConstructor(Double value) {
            this.value = value;
        }

        public Double getValue() {
            return value;
        }
    }
}
