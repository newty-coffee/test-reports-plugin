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

package org.newtco.test.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CodeGen {

    /**
     * The Signature class represents the signature of an executable (method or constructor) in the form of a string,
     * along with its necessary imports and parameter names.
     */
    public static class Signature {

        private final List<String> imports;
        private final List<String> parameterNames;
        private final String       signature;

        private Signature(Set<String> imports, List<String> parameterNames, String signature) {
            this.imports        = List.copyOf(imports);
            this.parameterNames = List.copyOf(parameterNames);
            this.signature      = signature;
        }

        /**
         * Creates a Signature object from the provided Executable instance.
         *
         * @param executable the Executable (either a Method or Constructor) for which the signature is to be created
         * @return a Signature object representing the signature of the provided Executable
         */
        public static Signature of(Executable executable) {
            return new Builder().build(executable, null);
        }

        /**
         * Creates a Signature object from the provided Executable instance renamed to the provided name.
         *
         * @param executable the Executable (either a Method or Constructor) for which the signature is to be created
         * @param newName    the new constructor or method name for the signature
         * @return a Signature object representing the signature of the provided Executable
         */
        public static Signature of(Executable executable, String newName) {
            return new Builder().build(executable, newName);
        }

        public String getSignature() {
            return signature;
        }

        public List<String> getImports() {
            return imports;
        }

        public List<String> getParameterNames() {
            return parameterNames;
        }

        private static class Builder {
            private final Set<String>  imports;
            private final List<String> parameterNames;

            public Builder() {
                this.imports        = new TreeSet<>();
                this.parameterNames = new ArrayList<>();
            }

            public Signature build(Executable executable, String name) {
                var signature = new StringBuilder();

                // Constructor/Method annotations
                String methodAnnotations = getAnnotations(executable);
                signature.append(methodAnnotations);

                // Modifiers
                appendModifiers(signature, executable);

                // Type parameters
                appendTypeParameters(signature, executable.getTypeParameters());

                // Return type for methods
                if (executable instanceof Method method) {
                    // Get annotations on the return type
                    String returnTypeAnnotations = getAnnotations(method.getAnnotatedReturnType());
                    Type   returnType            = method.getGenericReturnType();
                    signature.append(returnTypeAnnotations).append(getSimpleTypeName(returnType)).append(' ');
                }

                // Name of the method or constructor
                if (name != null) {
                    signature.append(name);
                } else {
                    if (executable instanceof Constructor) {
                        signature.append(executable.getDeclaringClass().getSimpleName());
                    } else if (executable instanceof Method) {
                        signature.append(executable.getName());
                    }
                }

                // Parameters
                signature.append('(');
                appendParameters(signature, executable);
                signature.append(')');

                // Exceptions
                appendExceptions(signature, executable.getGenericExceptionTypes());

                return new Signature(imports, parameterNames, signature.toString());
            }

            private void appendModifiers(StringBuilder signature, Executable executable) {
                int modifiers = executable.getModifiers();
                if (executable instanceof Method) {
                    // Only method modifiers
                    modifiers &= Modifier.methodModifiers();
                } else if (executable instanceof Constructor) {
                    modifiers &= Modifier.classModifiers();
                }

                var modifiersString = Modifier.toString(modifiers);
                if (!modifiersString.isEmpty()) {
                    signature.append(modifiersString).append(' ');
                }
            }

            private void appendTypeParameters(StringBuilder signature, TypeVariable<?>[] typeParams) {
                if (typeParams.length > 0) {
                    signature.append('<');
                    for (int i = 0; i < typeParams.length; i++) {
                        var typeParam = typeParams[i];
                        signature.append(typeParam.getName());
                        appendTypeBounds(signature, typeParam.getBounds(), " extends ");
                        if (i < typeParams.length - 1) {
                            signature.append(", ");
                        }
                    }
                    signature.append("> ");
                }
            }

            private void appendTypeBounds(StringBuilder sb, Type[] bounds, String prefix) {
                if (bounds.length > 0 && !(bounds.length == 1 && bounds[0] == Object.class)) {
                    sb.append(prefix);
                    for (int j = 0; j < bounds.length; j++) {
                        sb.append(getSimpleTypeName(bounds[j]));
                        if (j < bounds.length - 1) {
                            sb.append(" & ");
                        }
                    }
                }
            }

            private void appendParameters(StringBuilder signature, Executable executable) {
                Type[]      parameterTypes = executable.getGenericParameterTypes();
                Parameter[] parameters     = executable.getParameters();
                boolean     isVarArgs      = executable.isVarArgs();

                for (int i = 0; i < parameterTypes.length; i++) {
                    Parameter parameter = parameters[i];

                    // Annotations on parameters
                    String paramAnnotations = getAnnotations(parameter);
                    signature.append(paramAnnotations);

                    Type   paramType = parameterTypes[i];
                    String typeName  = getSimpleTypeName(paramType);

                    // Check if this parameter is the varargs parameter
                    if (isVarArgs && i == parameterTypes.length - 1) {
                        // Replace '[]' with '...'
                        if (typeName.endsWith("[]")) {
                            typeName = typeName.substring(0, typeName.length() - 2) + "...";
                        }
                    }

                    signature.append(typeName);

                    // Collect parameter names
                    var paramName = parameter.getName();
                    parameterNames.add(paramName);
                    signature.append(' ').append(paramName);

                    if (i < parameterTypes.length - 1) {
                        signature.append(", ");
                    }
                }
            }

            private void appendExceptions(StringBuilder signature, Type[] exceptionTypes) {
                if (exceptionTypes.length > 0) {
                    signature.append(" throws ");
                    for (int i = 0; i < exceptionTypes.length; i++) {
                        signature.append(getSimpleTypeName(exceptionTypes[i]));
                        if (i < exceptionTypes.length - 1) {
                            signature.append(", ");
                        }
                    }
                }
            }

            private String getSimpleTypeName(Type type) {
                if (type instanceof Class<?> cls) {
                    addImport(cls);
                    return cls.getSimpleName();
                } else if (type instanceof ParameterizedType pt) {
                    var sb = new StringBuilder();
                    sb.append(getSimpleTypeName(pt.getRawType()));
                    Type[] typeArgs = pt.getActualTypeArguments();
                    if (typeArgs.length > 0) {
                        sb.append('<');
                        for (int i = 0; i < typeArgs.length; i++) {
                            sb.append(getSimpleTypeName(typeArgs[i]));
                            if (i < typeArgs.length - 1) {
                                sb.append(", ");
                            }
                        }
                        sb.append('>');
                    }
                    return sb.toString();
                } else if (type instanceof TypeVariable<?> tv) {
                    return tv.getName();
                } else if (type instanceof WildcardType wt) {
                    var sb = new StringBuilder().append('?');
                    appendTypeBounds(sb, wt.getLowerBounds(), " super ");
                    appendTypeBounds(sb, wt.getUpperBounds(), " extends ");
                    return sb.toString();
                } else if (type instanceof GenericArrayType gat) {
                    return getSimpleTypeName(gat.getGenericComponentType()) + "[]";
                } else {
                    return type.toString();
                }
            }

            private String getAnnotations(AnnotatedElement element) {
                StringBuilder sb = new StringBuilder();

                for (Annotation annotation : element.getAnnotations()) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    addImport(annotationType);
                    sb.append('@').append(annotationType.getSimpleName()).append(' ');
                }
                return sb.toString();
            }


            private void addImport(Class<?> cls) {
                if (cls.isArray()) {
                    addImport(cls.getComponentType());
                } else if (!cls.isPrimitive() && !cls.getPackageName().equals("java.lang")) {
                    imports.add(cls.getCanonicalName());
                }
            }
        }
    }
}
