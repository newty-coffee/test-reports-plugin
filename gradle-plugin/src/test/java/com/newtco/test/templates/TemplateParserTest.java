package com.newtco.test.templates;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateParserTest {

    @Test
    public void testParse_onlyText() {
        TemplateParser                    parser          = new TemplateParser();
        String                            templateContent = "This is plain text.";
        List<TemplateParser.TemplatePart> parts           = parser.parse(templateContent);

        assertEquals(1, parts.size());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(0).getType());
        assertEquals("This is plain text.", parts.get(0).getContent());
    }

    @Test
    public void testParse_expression() {
        TemplateParser                    parser          = new TemplateParser();
        String                            templateContent = "Hello, <%= name %>!";
        List<TemplateParser.TemplatePart> parts           = parser.parse(templateContent);

        assertEquals(3, parts.size());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(0).getType());
        assertEquals("Hello, ", parts.get(0).getContent());
        assertEquals(TemplateParser.PartType.EXPRESSION, parts.get(1).getType());
        assertEquals(" name ", parts.get(1).getContent());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(2).getType());
        assertEquals("!", parts.get(2).getContent());
    }

    @Test
    public void testParse_import() {
        TemplateParser                    parser          = new TemplateParser();
        String                            templateContent = "import java.util.List;\nSome content.";
        List<TemplateParser.TemplatePart> parts           = parser.parse(templateContent);

        assertEquals(2, parts.size());
        assertEquals(TemplateParser.PartType.IMPORT, parts.get(0).getType());
        assertEquals("java.util.List", parts.get(0).getContent());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(1).getType());
        assertEquals("Some content.", parts.get(1).getContent());
    }

    @Test
    public void testParse_code() {
        TemplateParser                    parser          = new TemplateParser();
        String                            templateContent = "<% for (int i = 0; i < 5; i++) { %>loop text<% } %>";
        List<TemplateParser.TemplatePart> parts           = parser.parse(templateContent);

        assertEquals(3, parts.size());
        assertEquals(TemplateParser.PartType.CODE, parts.get(0).getType());
        assertEquals(" for (int i = 0; i < 5; i++) { ", parts.get(0).getContent());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(1).getType());
        assertEquals("loop text", parts.get(1).getContent());
        assertEquals(TemplateParser.PartType.CODE, parts.get(2).getType());
        assertEquals(" } ", parts.get(2).getContent());
    }

    @Test
    public void testParse_mixedContent() {
        TemplateParser                    parser          = new TemplateParser();
        String                            templateContent = "Text before <% code block %> text after <%= expression %>.";
        List<TemplateParser.TemplatePart> parts           = parser.parse(templateContent);

        assertEquals(5, parts.size());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(0).getType());
        assertEquals("Text before ", parts.get(0).getContent());
        assertEquals(TemplateParser.PartType.CODE, parts.get(1).getType());
        assertEquals(" code block ", parts.get(1).getContent());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(2).getType());
        assertEquals(" text after ", parts.get(2).getContent());
        assertEquals(TemplateParser.PartType.EXPRESSION, parts.get(3).getType());
        assertEquals(" expression ", parts.get(3).getContent());
        assertEquals(TemplateParser.PartType.TEXT, parts.get(4).getType());
        assertEquals(".", parts.get(4).getContent());
    }
}