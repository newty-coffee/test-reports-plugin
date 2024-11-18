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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A class for writing JSON content using a provided Writer object. The JsonWriter class provides functionality to write
 * fields, values, arrays, and raw JSON content to the underlying writer.
 */
@NotThreadSafe
public class JsonWriter {

    private static final char[] HexChars = "0123456789ABCDEF".toCharArray();

    protected Writer writer;

    public JsonWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Adds a JSON field to the writer with the specified name.
     *
     * @param name the name of the JSON field
     * @return the updated JsonWriter instance
     */
    public JsonWriter field(String name) {
        return raw("\"", name, "\":");
    }

    /**
     * Adds a JSON value to the writer. If the value is null, it writes 'null'.
     *
     * @param value the string value to write
     * @return the updated JsonWriter instance
     */
    public JsonWriter value(String value) {
        if (value == null) {
            return raw("null");
        }
        return raw("\"", escapeJson(value), "\"");
    }

    /**
     * Adds a JSON numeric value to the writer.
     *
     * @param value the long value to write
     * @return the updated JsonWriter instance
     */
    public JsonWriter value(long value) {
        return raw(Long.toString(value));
    }

    /**
     * Adds a JSON boolean value to the writer.
     *
     * @param value the boolean value to write
     * @return the updated JsonWriter instance
     */
    public JsonWriter value(boolean value) {
        return raw(Boolean.toString(value));
    }

    /**
     * Adds a JSON double value to the writer.
     *
     * @param value the double value to write
     * @return the updated JsonWriter instance
     */
    public JsonWriter value(double value) {
        return raw(Double.toString(value));
    }

    /**
     * Process and adds a custom value to the writer using a given context and processor.
     *
     * @param <T> the type of the context
     * @param context the context to process
     * @param processor the processor that writes the context to the writer
     * @return the updated JsonWriter instance
     */
    public <T> JsonWriter value(T context, Consumer<T> processor) {
        processor.accept(context);
        return this;
    }

    /**
     * Adds a JSON array to the writer using an iterator and processor. The processor formats each item in the array.
     *
     * @param <T> the type of elements in the iterator
     * @param iterator the iterator of elements to write
     * @param processor the processor that writes each element to the writer
     * @return the updated JsonWriter instance
     */
    public <T> JsonWriter array(Iterator<T> iterator, Consumer<T> processor) {
        raw('[');
        if (iterator.hasNext()) {
            var items = new ArrayList<String>();

            while (iterator.hasNext()) {
                try (var override = new OverrideWriter()) {
                    processor.accept(iterator.next());

                    var item = override.toString();
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }

            if (!items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    if (i > 0) {
                        comma();
                    }
                    raw(items.get(i));
                }
            }
        }
        raw(']');
        return this;
    }

    /**
     * Adds a JSON array to the writer using an iterator, context, and processor.
     * The processor formats each item in the array using the provided context.
     *
     * @param <T> the type of elements in the iterator
     * @param <U> the type of context
     * @param iterator the iterator of elements to write
     * @param context the context used in the processor
     * @param processor the processor that writes each element with the context to the writer
     * @return the updated JsonWriter instance
     */
    public <T, U> JsonWriter array(Iterator<T> iterator, U context, BiConsumer<U, T> processor) {
        raw('[');

        if (iterator.hasNext()) {
            var items = new ArrayList<String>();

            while (iterator.hasNext()) {
                try (var override = new OverrideWriter()) {
                    processor.accept(context, iterator.next());

                    var item = override.toString();
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }

            if (!items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    if (i > 0) {
                        comma();
                    }
                    raw(items.get(i));
                }
            }
        }

        raw(']');
        return this;
    }

    /**
     * Adds a JSON array to the writer using a collection and processor.
     * The processor formats each item in the collection.
     *
     * @param <T> the type of elements in the collection
     * @param values the collection of elements to write
     * @param processor the processor that writes each element to the writer
     * @return the updated JsonWriter instance
     */
    public <T> JsonWriter array(Collection<T> values, Consumer<T> processor) {
        return array(values, null, (context, value) -> processor.accept(value));
    }

    /**
     * Adds a JSON array to the writer using a collection, context, and processor.
     * The processor formats each item in the collection using the provided context.
     *
     * @param <T> the type of elements in the collection
     * @param <U> the type of context
     * @param values the collection of elements to write
     * @param context the context used in the processor
     * @param processor the processor that writes each element with the context to the writer
     * @return the updated JsonWriter instance
     */
    public <T, U> JsonWriter array(Collection<T> values, U context, BiConsumer<U, T> processor) {
        raw('[');

        if (!values.isEmpty()) {
            var items = new ArrayList<String>();

            for (var value : values) {
                try (var override = new OverrideWriter()) {
                    processor.accept(context, value);

                    var item = override.toString();
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }

            if (!items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    if (i > 0) {
                        comma();
                    }
                    raw(items.get(i));
                }
            }
        }

        raw(']');

        return this;
    }

    /**
     * Adds a comma to the writer.
     *
     * @return the updated JsonWriter instance
     */
    public JsonWriter comma() {
        return raw(",");
    }

    /**
     * Adds a raw character to the writer.
     *
     * @param ch the character to write
     * @return the updated JsonWriter instance
     */
    public JsonWriter raw(char ch) {
        try {
            writer.append(ch);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    /**
     * Adds raw character sequences to the writer.
     *
     * @param values the character sequences to write
     * @return the updated JsonWriter instance
     */
    public JsonWriter raw(CharSequence... values) {
        for (var value : values) {
            try {
                writer.append(value);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return this;
    }

    String escapeJson(String value) {
        if (value == null) {
            return "null";
        }

        if (value.isEmpty()) {
            return "";
        }

        var escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            int cp = value.codePointAt(i);
            switch (cp) {
                case '\"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (cp <= 31 || cp >= 127) {
                        char[] seq = new char[6];
                        seq[0] = '\\';
                        seq[1] = 'u';
                        seq[2] = HexChars[(cp >> 12) & 0xF];
                        seq[3] = HexChars[(cp >> 8) & 0xF];
                        seq[4] = HexChars[(cp >> 4) & 0xF];
                        seq[5] = HexChars[cp & 0xF];
                        escaped.append(new String(seq));
                    } else {
                        escaped.append((char) cp);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    /**
     * Writer extension to buffer writes during collection generation which is necessary for adding commas
     */
    private class OverrideWriter extends Writer {
        final Writer        original;
        final StringBuilder buffer;

        public OverrideWriter() {
            this.buffer            = new StringBuilder();
            this.original          = JsonWriter.this.writer;
            JsonWriter.this.writer = this;
        }

        @Override
        public void write(@Nonnull char[] cbuf, int off, int len) {
            buffer.append(cbuf, off, len);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            JsonWriter.this.writer = original;
        }

        @Override
        public String toString() {
            return buffer.toString();
        }
    }
}
