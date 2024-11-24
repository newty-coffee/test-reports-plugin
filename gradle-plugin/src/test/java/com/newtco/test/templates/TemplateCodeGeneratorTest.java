package com.newtco.test.templates;

import com.google.common.truth.Truth;
import com.newtco.test.reports.api.Template;
import org.junit.jupiter.api.Test;

import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class TemplateCodeGeneratorTest {

    @Test
    public void testGenerateTemplateClassWithTextPart() {
        TemplateCodeGenerator generator   = new TemplateCodeGenerator();
        String                packageName = "com.newtco.generated";
        String                className   = "GeneratedClass";
        Class<?>              parentClass = TestTemplate.class;
        List<TemplateParser.TemplatePart> parts = List.of(
                new TemplateParser.TemplatePart(TemplateParser.PartType.TEXT, "Hello, World!")
        );
        String result = generator.generateTemplateClass(packageName, className, parentClass, parts);

        Truth.assertThat(result).contains("package com.newtco.generated;");
        Truth.assertThat(result).contains("import com.newtco.test.templates.TemplateCodeGeneratorTest.TestTemplate;");
        Truth.assertThat(result).contains("public class GeneratedClass extends TestTemplate<GeneratedClass> {");
        Truth.assertThat(result).contains("out(\"Hello, World!\");");
    }

    @Test
    public void testGenerateTemplateClassWithExpressionPart() {
        TemplateCodeGenerator generator   = new TemplateCodeGenerator();
        String                packageName = "com.newtco.generated";
        String                className   = "GeneratedClass";
        Class<?>              parentClass = TestTemplate.class;
        List<TemplateParser.TemplatePart> parts = List.of(
                new TemplateParser.TemplatePart(TemplateParser.PartType.EXPRESSION, "2 + 2")
        );
        String result = generator.generateTemplateClass(packageName, className, parentClass, parts);

        Truth.assertThat(result).contains("package com.newtco.generated;");
        Truth.assertThat(result).contains("import com.newtco.test.templates.TemplateCodeGeneratorTest.TestTemplate;");
        Truth.assertThat(result).contains("public class GeneratedClass extends TestTemplate<GeneratedClass> {");
        Truth.assertThat(result).contains("out(2 + 2);");
    }

    @Test
    public void testGenerateTemplateClassWithCodePart() {
        TemplateCodeGenerator generator   = new TemplateCodeGenerator();
        String                packageName = "com.newtco.generated";
        String                className   = "GeneratedClass";
        Class<?>              parentClass = TestTemplate.class;
        List<TemplateParser.TemplatePart> parts = List.of(
                new TemplateParser.TemplatePart(TemplateParser.PartType.CODE, "int x = 10; out(x);")
        );
        String result = generator.generateTemplateClass(packageName, className, parentClass, parts);

        Truth.assertThat(result).contains("package com.newtco.generated;");
        Truth.assertThat(result).contains("import com.newtco.test.templates.TemplateCodeGeneratorTest.TestTemplate;");
        Truth.assertThat(result).contains("public class GeneratedClass extends TestTemplate<GeneratedClass> {");
        Truth.assertThat(result).contains("int x = 10;");
        Truth.assertThat(result).contains("out(x);");
    }

    @Test
    public void testGenerateTemplateClassWithMultipleParts() {
        TemplateCodeGenerator generator   = new TemplateCodeGenerator();
        String                packageName = "com.newtco.generated";
        String                className   = "GeneratedClass";
        Class<?>              parentClass = TestTemplate.class;
        List<TemplateParser.TemplatePart> parts = Arrays.asList(
                new TemplateParser.TemplatePart(TemplateParser.PartType.TEXT, "Hello, "),
                new TemplateParser.TemplatePart(TemplateParser.PartType.EXPRESSION, "name"),
                new TemplateParser.TemplatePart(TemplateParser.PartType.CODE,
                        "if (name == null) { name = \"World\"; } out(name);")
        );
        String result = generator.generateTemplateClass(packageName, className, parentClass, parts);

        Truth.assertThat(result).contains("package com.newtco.generated;");
        Truth.assertThat(result).contains("import com.newtco.test.templates.TemplateCodeGeneratorTest.TestTemplate;");
        Truth.assertThat(result).contains("public class GeneratedClass extends TestTemplate<GeneratedClass> {");
        Truth.assertThat(result).contains("out(\"Hello, \");");
        Truth.assertThat(result).contains("out(name);");
        Truth.assertThat(result).contains("if (name == null) { name = \"World\"; } out(name);");
    }


    // Mock Template class to be used in tests
    public static class TestTemplate extends Template<TestTemplate> {
        public TestTemplate(Writer writer) {
            super(writer);
        }

        @Override
        protected TestTemplate self() {
            return this;
        }

        @Override
        public void render() throws Exception {

        }
    }
}