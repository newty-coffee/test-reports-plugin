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

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Various utility helpers for Gradle specific code
 */
public class Gradle {

    private Gradle() {
    }

    /**
     * Return the first non-null project property or system environment variable matching name.
     *
     * @param project Project to search
     * @param names   property names or system environment variable names to search for. The first non-null value is
     *                returned.
     * @return Non-null value if found, else null
     */
    public static String findProperty(Project project, String... names) {
        for (String name : names) {
            var property = Objects.toString(project.findProperty(name), null);
            if (null == property) {
                var providers = project.getProviders();

                property = providers.systemProperty(name).getOrNull();
                if (null == property) {
                    property = providers.gradleProperty(name).getOrNull();
                    if (property == null) {
                        property = providers.environmentVariable(name).getOrNull();
                    }
                }
            }
            if (null != property) {
                return property;
            }
        }

        return null;
    }

    /**
     * Helpers to adapt multi-arg methods to {@code Action<>}
     */
    public interface Actions {

        /**
         * Adapts an Action type to a more generic Action type.
         *
         * @param <T>    the type of the generic action
         * @param <A>    a type that extends T and will be used in the provided action. Assumes that T can be safely
         *               cast to A.
         * @param action the action to be adapted
         * @return an Action instance of the more generic type T
         */
        @SuppressWarnings("unchecked")
        static <T, A extends T> Action<T> calling(Action<A> action) {
            return (T a) -> action.execute((A) a);
        }

        /**
         * Adapts a BiAction to an Action, allowing it to be executed with a fixed second parameter.
         *
         * @param <T>    the type of the generic action
         * @param <A>    a type that extends T and will be used in the provided BiAction. Assumes that T can be safely
         *               cast to A.
         * @param <B>    the type of the second parameter in the BiAction
         * @param action the BiAction to be adapted
         * @param b      the second parameter to be provided to the BiAction when the generated Action is executed
         * @return an Action instance of the more generic type T
         */
        @SuppressWarnings("unchecked")
        static <T, A extends T, B> Action<T> calling(BiAction<A, B> action, B b) {
            return (T a) -> action.execute((A) a, b);
        }

        /**
         * Adapts a TriAction to an Action, allowing it to be executed with fixed second and third parameters.
         *
         * @param <T>    the type of the generic action
         * @param <A>    a type that extends T and will be used in the provided TriAction. Assumes that T can be safely
         *               cast to A.
         * @param <B>    the type of the second parameter in the TriAction
         * @param <C>    the type of the third parameter in the TriAction
         * @param action the TriAction to be adapted
         * @param b      the second parameter to be provided to the TriAction when the generated Action is executed
         * @param c      the third parameter to be provided to the TriAction when the generated Action is executed
         * @return an Action instance of the more generic type T
         */
        @SuppressWarnings("unchecked")
        static <T, A extends T, B, C> Action<T> calling(TriAction<A, B, C> action, B b, C c) {
            return (T a) -> action.execute((A) a, b, c);
        }

        /**
         * Functional interface that represents an action to be performed on an object of type A.
         * <p>
         * This interface extends the org.gradle.api.Action interface and is Serializable. It can be used to define
         * custom actions that can be executed on instances of the specified type.
         *
         * @param <A> the type of object the action will be performed on
         */
        interface Action<A> extends org.gradle.api.Action<A>, Serializable {

            void execute(@Nonnull A a);
        }

        /**
         * Functional interface that represents an action to be performed on two objects of types A and B.
         * <p>
         * This interface is intended to be used where an operation needs to accept exactly two parameters and perform
         * some action on them.
         *
         * @param <A> the type of the first parameter
         * @param <B> the type of the second parameter
         */
        interface BiAction<A, B> {

            void execute(A a, B b);
        }

        /**
         * Functional interface that represents an action to be performed on three objects of types A, B, and C.
         * <p>
         * This interface is intended to be used where an operation needs to accept exactly three parameters and perform
         * some action on them.
         *
         * @param <A> the type of the first parameter
         * @param <B> the type of the second parameter
         * @param <C> the type of the third parameter
         */
        interface TriAction<A, B, C> extends Serializable {

            void execute(A a, B b, C c);
        }
    }

    public interface Extensions {

        /**
         * Retrieves an extension of a specified type from the given container.
         *
         * @param <C>           the type of the container, which must extend `ExtensionAware`
         * @param <E>           the type of the extension to be retrieved
         * @param container     the container that holds the extensions
         * @param extensionType the class type of the extension to be retrieved
         * @return the extension of the specified type
         */
        static <C extends ExtensionAware, E> E extensionOf(C container, Class<E> extensionType) {
            return container.getExtensions().getByType(extensionType);
        }
    }
}



