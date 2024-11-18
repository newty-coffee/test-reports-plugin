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

package com.newtco.test.templates;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.newtco.test.util.CodeGen;

/**
 * TemplateCodeGenerator is responsible for generating Java source code for a class that extends a specified template
 * base class. The generated class includes methods to output template parts such as text, expressions, and code.
 * <p>
 * The primary method {@code generateJavaSource} takes a class name and a list of template parts and returns the
 * corresponding Java source code as a String.
 */
public class TemplateCodeGenerator {


    public TemplateCodeGenerator() {

    }

    /**
     * Generates the Java source code for a given class and template parts.
     *
     * @param className The name of the class to be generated.
     * @param parts     The list of template parts used to generate the source code. Each part can be static text, an
     *                  expression, or a code block.
     *
     * @return The generated Java source code as a string.
     */
    public String generateTemplateClass(
        String packageName,
        String className, 
        Class<?> parentClass,
        List<TemplateParser.TemplatePart> parts) {

        var source = new StringBuilder();

        // Package and imports
        source.append("package ").append(packageName).append(";\n\n")
            .append("\n")
            .append("import ").append(parentClass.getCanonicalName()).append(";\n");

        for (TemplateParser.TemplatePart part : parts) {
            if (part.type == TemplateParser.PartType.IMPORT) {
                source.append("import ").append(part.content).append(";\n");
            }
        }

        var signature = getConstructorSignature(className, parentClass);


        // Add imports for parent class types
        for (var name : signature.getImports()) {
            source.append("import ").append(name).append(";\n");
        }

        source.append("\n\n")
            .append("public class ").append(className)
            .append(" extends ").append(parentClass.getSimpleName()).append("<").append(className).append("> {\n")
            .append("  ").append(signature.getSignature()).append(" {\n")
            .append("    super(").append(String.join(", ", signature.getParameterNames())).append(");\n")
            .append("  }\n\n")
            .append("  @Override\n")
            .append("  public ").append(className).append(" self() {\n")
            .append("    return this;\n")
            .append("  }\n\n")
            .append("  @Override\n")
            .append("  public void render() throws Exception {\n");


        // Generate code for each template part
        for (TemplateParser.TemplatePart part : parts) {
            if (part.type == TemplateParser.PartType.TEXT) {
                var text =  escapeJavaString(part.content);
                source.append("    out(\"").append(text).append("\");\n");
            } else if (part.type == TemplateParser.PartType.EXPRESSION) {
                source.append("    out(").append(part.content.trim()).append(");\n");
            } else if (part.type == TemplateParser.PartType.CODE) {
                source.append(indent("    ", part.content)).append("\n");
            }
        }

        // Close method and class
        source.append("  }\n");
        source.append("}\n");

        return source.toString();
    }

    private CodeGen.Signature getConstructorSignature(String className, Class<?> template) {
        var constructors = template.getConstructors();
        if (constructors.length != 1) {
            throw new UnsupportedOperationException(
                "Expected single constructor for template class %s. Found %d".formatted(
                    template.getName(), constructors.length));
        }

        return CodeGen.Signature.of(constructors[0], className);
    }

    private String indent(String spaces, String text) {
        var indented = new StringBuilder();
        for (var line : text.split("\n")) {
            indented.append(spaces).append(line).append("\n");
        }
        return indented.toString();
    }

    private String escapeJavaString(String str) {
        var result = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\\':
                    result.append("\\\\");
                    break;
                case '\"':
                    result.append("\\\"");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 32 || c > 126) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }
}
