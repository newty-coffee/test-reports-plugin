package com.newtco.test.util;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonWriterTest {

    @Test
    public void testValue_String() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value("hello");

        assertEquals("\"hello\"", writer.toString());
    }

    @Test
    public void testValue_Long() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value(12345L);

        assertEquals("12345", writer.toString());
    }

    @Test
    public void testValue_BooleanTrue() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value(true);

        assertEquals("true", writer.toString());
    }

    @Test
    public void testValue_BooleanFalse() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value(false);

        assertEquals("false", writer.toString());
    }

    @Test
    public void testValue_Double() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value(3.14);

        assertEquals("3.14", writer.toString());
    }

    @Test
    public void testValue_NullString() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value((String) null);

        assertEquals("null", writer.toString());
    }

    // Test for consumer value method
    @Test
    public void testValue_Consumer() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.value("context", ctx -> jsonWriter.raw(ctx));

        assertEquals("context", writer.toString());
    }

    @Test
    public void testField_String() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.field("name").value("John");

        assertEquals("\"name\":\"John\"", writer.toString());
    }

    @Test
    public void testField_EscapedString() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.field("escaped").value("This is a \"quote\"");

        assertEquals("\"escaped\":\"This is a \\\"quote\\\"\"", writer.toString());
    }

    @Test
    public void testField_EmptyString() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.field("empty").value("");

        assertEquals("\"empty\":\"\"", writer.toString());
    }

    @Test
    public void testField_Nested() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);

        jsonWriter.field("nested").value("{\"inner\":\"value\"}");

        assertEquals("\"nested\":\"{\\\"inner\\\":\\\"value\\\"}\"", writer.toString());
    }

    @Test
    public void testArray_IteratorAndConsumer() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);
        List<String> items      = Arrays.asList("one", "two", "three");

        jsonWriter.array(items.iterator(), jsonWriter::value);

        assertEquals("[\"one\",\"two\",\"three\"]", writer.toString());
    }

    @Test
    public void testArray_IteratorAndBiConsumer() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);
        List<String> items      = Arrays.asList("one", "two", "three");

        jsonWriter.array(items.iterator(), "prefix", (prefix, item) -> jsonWriter.value(prefix + item));

        assertEquals("[\"prefixone\",\"prefixtwo\",\"prefixthree\"]", writer.toString());
    }

    @Test
    public void testArray_CollectionAndConsumer() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);
        List<String> items      = Arrays.asList("one", "two", "three");

        jsonWriter.array(items, jsonWriter::value);

        assertEquals("[\"one\",\"two\",\"three\"]", writer.toString());
    }

    @Test
    public void testArray_CollectionAndNonWritingConsumer() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);
        List<String> items      = Arrays.asList("one", "two", "three");

        jsonWriter.array(items, v -> {
            if ("two".equals(v)) {
                jsonWriter.value(v);
            }
        });

        assertEquals("[\"two\"]", writer.toString());
    }

    @Test
    public void testArray_emptyCollection() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);
        List<String> items      = List.of();

        jsonWriter.array(items, jsonWriter::value);

        assertEquals("[]", writer.toString());
    }

    @Test
    public void testArray_CollectionAndBiConsumer() {
        StringWriter writer     = new StringWriter();
        JsonWriter   jsonWriter = new JsonWriter(writer);
        List<String> items      = Arrays.asList("one", "two", "three");

        jsonWriter.array(items, "prefix", (prefix, item) -> jsonWriter.value(prefix + item));

        assertEquals("[\"prefixone\",\"prefixtwo\",\"prefixthree\"]", writer.toString());
    }

    @Test
    public void testEscapeJson_String() {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter());
        String     result     = jsonWriter.escapeJson("hello");
        assertEquals("hello", result);
    }

    @Test
    public void testEscapeJson_SpecialCharacters() {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter());
        String     result     = jsonWriter.escapeJson("This is a \"quote\" and \\backslash");
        assertEquals("This is a \\\"quote\\\" and \\\\backslash", result);
    }

    @Test
    public void testEscapeJson_ControlCharacters() {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter());
        String     result     = jsonWriter.escapeJson("\n \t \b \f \r");
        assertEquals("\\n \\t \\b \\f \\r", result);
    }

    @Test
    public void testEscapeJson_UnicodeCharacters() {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter());
        String     result     = jsonWriter.escapeJson("Unicode test: \u20AC \u0001"); // Euro Sign
        assertEquals("Unicode test: \\u20AC \\u0001", result);
    }

    @Test
    public void testEscapeJson_EmptyString() {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter());
        String     result     = jsonWriter.escapeJson("");
        assertEquals("", result);
    }

    @Test
    public void testEscapeJson_Null() {
        JsonWriter jsonWriter = new JsonWriter(new StringWriter());
        String     result     = jsonWriter.escapeJson(null);
        assertEquals("null", result);
    }
}