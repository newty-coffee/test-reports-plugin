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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.reflect.ObjectInstantiationException;

/**
 * Utility class for performing operations related to object instantiation and reflection.
 */
public class Reflect {

    /**
     * Creates a new instance of the specified class using the provided parameters. If the parameter types do not
     * exactly match a constructor, an attempt to locate a compatible one will be made.
     *
     * @param <T>        the type of the object to be created
     * @param clazz      the Class object representing the class to be instantiated
     * @param parameters the parameters to be passed to the constructor
     *
     * @return an instance of the specified class
     *
     * @throws ObjectInstantiationException if the instance creation fails due to an underlying issue
     */
    public static <T> T newInstance(Class<? extends T> clazz, Object... parameters) throws ObjectInstantiationException {
        try {
            return getConstructor(clazz, parameters).newInstance(parameters);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new ObjectInstantiationException(clazz, e.getCause());
        } catch (Throwable e) {
            throw new ObjectInstantiationException(clazz, e);
        }
    }

    /**
     * Creates a new instance of the specified class using the provided parameters. If the parameter types do not
     * exactly match a constructor, an attempt to locate a compatible one will be made.
     *
     * @param <T>         the type of the object to be created
     * @param constructor the Class constructor
     * @param parameters  the parameters to be passed to the constructor
     *
     * @return an instance of the specified class
     *
     * @throws ObjectInstantiationException if the instance creation fails due to an underlying issue
     */
    public static <T> T newInstance(Constructor<? extends T> constructor, Object... parameters) throws ObjectInstantiationException {
        try {
            return constructor.newInstance(parameters);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new ObjectInstantiationException(constructor.getDeclaringClass(), e.getCause());
        } catch (Throwable e) {
            throw new ObjectInstantiationException(constructor.getDeclaringClass(), e);
        }
    }

    /**
     * Retrieves the constructor of the specified class that matches the given parameter types. If the parameter types
     * do not exactly match a constructor, an attempt to locate a compatible constructor will be made.
     *
     * @param <T>        the type of the class for which the constructor is to be retrieved
     * @param clazz      the Class object representing the class whose constructor is to be retrieved
     * @param parameters the parameters to be passed to the constructor
     *
     * @return the Constructor object of the class matching the specified parameter types
     *
     * @throws NoSuchMethodException if a matching constructor is not found
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<? extends T> getConstructor(Class<? extends T> clazz, Object... parameters) throws NoSuchMethodException {

        // Populate parameterTypes with classes of parameters
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null) {
                throw new IllegalArgumentException("Parameter at index " + i + " is null. Null values are not supported.");
            }
            parameterTypes[i] = parameters[i].getClass();
        }

        try {
            // Try to find the constructor exactly matching the parameter types
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            // Continue to check compatibility if exact match not found
        }

        // Iterate through each constructor and check for compatibility
        for (Constructor<?> constructor : clazz.getConstructors()) {
            var constructorParameterTypes = constructor.getParameterTypes();
            if (constructorParameterTypes.length == parameters.length) {
                boolean isCompatible = true;
                for (int i = 0; i < parameters.length; i++) {
                    if (!isCompatibleType(constructorParameterTypes[i], parameterTypes[i])) {
                        isCompatible = false;
                        break;
                    }
                }
                if (isCompatible) {
                    return (Constructor<? extends T>) constructor;
                }
            }
        }

        // If no compatible constructor is found, throw an exception
        throw new NoSuchMethodException(Stream.of(parameterTypes)
            .map(Class::getName)
            .collect(Collectors.joining(",", clazz.getName() + "<init>.(", ")")));
    }

    /**
     * Checks if the provided argument can be assigned to the specified parameter type.
     *
     * @param constructorParameterType the type of the parameter
     * @param parameterType            the argument type to check
     *
     * @return true if the argument is compatible with the parameter type, false otherwise
     */
    private static boolean isCompatibleType(Class<?> constructorParameterType, Class<?> parameterType) {
        // Direct comparison for exact match and primitive types
        if (constructorParameterType.isAssignableFrom(parameterType)) {
            return true;
        }

        // Handle boxing and unboxing
        if (constructorParameterType.isPrimitive()) {
            return getWrapperType(constructorParameterType).isAssignableFrom(parameterType);
        } else {
            return getPrimitiveType(constructorParameterType) == parameterType;
        }
    }

    /**
     * Returns the wrapper type corresponding to the given primitive type.
     *
     * @param type the primitive type
     *
     * @return the corresponding wrapper type
     */
    private static Class<?> getWrapperType(Class<?> type) {
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        throw new IllegalArgumentException("Unsupported primitive type: " + type.getName());
    }

    /**
     * Returns the primitive type corresponding to the given wrapper type.
     *
     * @param type the wrapper type
     *
     * @return the corresponding primitive type, or null if the type is not a wrapper type
     */
    private static Class<?> getPrimitiveType(Class<?> type) {
        if (type == Boolean.class) return boolean.class;
        if (type == Byte.class) return byte.class;
        if (type == Character.class) return char.class;
        if (type == Short.class) return short.class;
        if (type == Integer.class) return int.class;
        if (type == Long.class) return long.class;
        if (type == Float.class) return float.class;
        if (type == Double.class) return double.class;
        return null; // Not a wrapper type
    }
}
