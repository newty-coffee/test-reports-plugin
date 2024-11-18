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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Generated;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.newtco.test.util.CodeGen.Signature;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SignatureTest {
    @Test
    @DisplayName("Test Simple Constructor")
    public void testSimpleConstructor() throws NoSuchMethodException {
        Constructor<?> constructor = A.class.getConstructor();
        Signature      signature   = Signature.of(constructor);

        String expectedSignature = "public A()";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Simple Renamed Constructor")
    public void testSimpleRenamedConstructor() throws NoSuchMethodException {
        Constructor<?> constructor = A.class.getConstructor();
        Signature      signature   = Signature.of(constructor, "B");

        String expectedSignature = "public B()";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Constructor with Primitives")
    public void testConstructorWithPrimitives() throws NoSuchMethodException {
        Constructor<?> constructor = A.class.getConstructor(int.class, double.class);
        Signature      signature   = Signature.of(constructor);

        String expectedSignature = "public A(int intValue, double doubleValue)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("intValue", "doubleValue");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Constructor with Generic Parameter and Exception")
    public void testConstructorWithGenericParameterAndException() throws NoSuchMethodException {
        Constructor<?> constructor = A.class.getConstructor(Number.class);
        Signature      signature   = Signature.of(constructor);

        String expectedSignature = "public A(T value) throws IllegalArgumentException";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of(); // java.lang.IllegalArgumentException is in java.lang
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("value");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Constructor with Bounded Type Parameter")
    public void testConstructorWithBoundedTypeParameter() throws NoSuchMethodException {
        Constructor<?> constructor = A.class.getConstructor(List.class);
        Signature      signature   = Signature.of(constructor);

        String expectedSignature = "public <E extends Exception> A(List<? super T> list) throws E";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of("java.util.List");
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("list");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with No Parameters")
    public void testMethodWithNoParameters() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("simpleMethod");
        Signature signature = Signature.of(method);

        String expectedSignature = "public void simpleMethod()";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Renamed Method")
    public void testRenamedMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("simpleMethod");
        Signature signature = Signature.of(method, "simpleMethod2");

        String expectedSignature = "public void simpleMethod2()";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Primitive and Array Parameters")
    public void testMethodWithArrayParameters() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodWithArray", int[].class, String[].class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public int methodWithArray(int[] numbers, String... strings)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("numbers", "strings");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Generic Return Type and Wildcards")
    public void testMethodWithGenerics() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodWithGenerics", Map.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public List<? extends T> methodWithGenerics(Map<String, ? super T> map)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of("java.util.List", "java.util.Map");
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("map");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Type Variable and Recursive Bound")
    public void testRecursiveMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("recursiveMethod", Comparable.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public <U extends Comparable<U>> U recursiveMethod(U param)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of(); // java.lang.Comparable is in java.lang
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("param");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Multiple Type Parameters and Exceptions")
    public void testComplexMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("complexMethod", Collection.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public <K, V extends Collection<K>> void complexMethod(V collection) throws IOException, NullPointerException";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of("java.util.Collection", "java.io.IOException");
        assertEquals(new HashSet<>(expectedImports), new HashSet<>(signature.getImports()));

        List<String> expectedParamNames = List.of("collection");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Static Method with Type Parameter")
    public void testStaticGenericMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("staticGenericMethod", Object.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public static <S> S staticGenericMethod(S param)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("param");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Annotated Parameter")
    public void testMethodWithAnnotations() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodWithAnnotations", String.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public void methodWithAnnotations(@PA String param)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of(
            A.PA.class.getCanonicalName()
        );
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("param");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method Throwing Custom Exception")
    public void testMethodThrowingCustomException() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodThrowingException");
        Signature signature = Signature.of(method);

        String expectedSignature = "public void methodThrowingException() throws CustomException";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of(
            A.CustomException.class.getCanonicalName()
        );
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Generic Array Return Type")
    public void testMethodReturningArray() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodReturningArray", Object.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public <E> E[] methodReturningArray(E element)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("element");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Synchronized Method")
    public void testSynchronizedMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("synchronizedMethod");
        Signature signature = Signature.of(method);

        String expectedSignature = "public synchronized void synchronizedMethod()";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of();
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Enums")
    public void testMethodWithEnum() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodWithEnum", A.CustomEnum.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public void methodWithEnum(CustomEnum customEnum)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of(
            A.CustomEnum.class.getCanonicalName()
        );
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of("customEnum");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Recursive Generic Type Parameter")
    public void testRecursiveGenericMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("recursiveGenericMethod", Comparable.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public <R extends Comparable<R> & Serializable> void recursiveGenericMethod(R param)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of("java.io.Serializable");
        assertEquals(new HashSet<>(expectedImports), new HashSet<>(signature.getImports()));

        List<String> expectedParamNames = List.of("param");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Generic Bounded by Multiple Interfaces")
    public void testMultiBoundMethod() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("multiBoundMethod", Runnable.class);
        Signature signature = Signature.of(method);

        String expectedSignature = "public <M extends Runnable & Closeable> void multiBoundMethod(M param)";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of("java.io.Closeable"); // java.lang.Runnable is in java.lang
        assertEquals(new HashSet<>(expectedImports), new HashSet<>(signature.getImports()));

        List<String> expectedParamNames = List.of("param");
        assertEquals(expectedParamNames, signature.getParameterNames());
    }

    @Test
    @DisplayName("Test Method with Annotated Return Type")
    public void testMethodWithAnnotatedReturnType() throws NoSuchMethodException {
        Method    method    = A.class.getMethod("methodWithAnnotatedReturnType");
        Signature signature = Signature.of(method);

        String expectedSignature = "public @TA String methodWithAnnotatedReturnType()";
        assertEquals(expectedSignature, signature.getSignature());

        List<String> expectedImports = List.of(
            A.TA.class.getCanonicalName()
        );
        assertEquals(expectedImports, signature.getImports());

        List<String> expectedParamNames = List.of();
        assertEquals(expectedParamNames, signature.getParameterNames());
    }


    /**
     * A test class demonstrating various method and constructor signatures, including generics, exceptions, and
     * annotations. All CodeGen.Signature unit tests are done against this class, so we can control required reflection
     * information.
     */
    @SuppressWarnings({"unused", "RedundantThrows"})
    private static class A<T extends Number & Comparable<T>> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        // Simple Constructor
        public A() {
        }

        // Constructor with Primitive Parameters
        public A(int intValue, double doubleValue) {
        }

        // Constructor with Generic Parameter and Exception
        public A(T value) throws IllegalArgumentException {
        }

        // Constructor with Bounded Type Parameter
        public <E extends Exception> A(List<? super T> list) throws E {
        }

        // Constructor with Recursive Type Variable
        public <E extends A<T> & Serializable> A(E instance) {
        }

        // Method with No Parameters
        public void simpleMethod() {
        }

        // Method with Primitive and Array Parameters
        public int methodWithArray(int[] numbers, String... strings) {
            return numbers.length + strings.length;
        }

        // Method with Generic Return Type and Wildcards
        public List<? extends T> methodWithGenerics(Map<String, ? super T> map) {
            return new ArrayList<>();
        }

        // Method with Type Variable and Recursive Bound
        public <U extends Comparable<U>> U recursiveMethod(U param) {
            return param;
        }

        // Method with Multiple Type Parameters and Exceptions
        public <K, V extends Collection<K>> void complexMethod(V collection) throws IOException, NullPointerException {
        }

        // Static Method with Type Parameter
        public static <S> S staticGenericMethod(S param) {
            return param;
        }

        // Private Method with Bounded Wildcard
        private List<? super Number> privateMethod(List<? extends Number> numbers) {
            return new ArrayList<>();
        }

        // Method with Annotated Parameter
        public void methodWithAnnotations(@PA String param) {
        }

        // Nested Class with Generic Type
        public static class NestedClass<N extends Serializable> {
            public void nestedMethod(N param) {
            }
        }

        // Method Throwing Custom Exception
        public void methodThrowingException() throws CustomException {
        }

        // Varargs Constructor with Generic Type
        @SafeVarargs
        public A(T... values) {
        }

        // Method with Generic Array Return Type
        @SuppressWarnings("unchecked")
        public <E> E[] methodReturningArray(E element) {
            return (E[]) new Object[]{element};
        }

        // Synchronized Method
        public synchronized void synchronizedMethod() {
        }

        // Native Method
        public native void nativeMethod();

        // Deprecated Method
        @Deprecated
        public void deprecatedMethod() {
        }

        // Final Method
        public final void finalMethod() {
        }

        // Method with Enums
        public void methodWithEnum(CustomEnum customEnum) {
        }


        // Method with Annotated Return Type
        public @TA String methodWithAnnotatedReturnType() {
            return "Non-null value";
        }

        // Enum Definition
        public enum CustomEnum {
        }

        // Custom Annotation Definition
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface PA {
        }

        // Custom Annotation Definition
        @Target(ElementType.TYPE_USE)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface TA {
        }


        // Custom Exception Class
        public static class CustomException extends Exception {
            @Serial
            public static final long serialVersionUID = 1L;
        }

        // Method with Recursive Generic Type Parameter
        public <R extends Comparable<R> & Serializable> void recursiveGenericMethod(R param) {
        }

        // Method with Generic Bounded by Multiple Interfaces
        public <M extends Runnable & Closeable> void multiBoundMethod(M param) {
        }
    }

}
