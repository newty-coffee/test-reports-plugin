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

package com.newtco.test.reports.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * An abstract base class for templates that provides methods for writing text to a {@code Writer}.
 *
 * @param <T> the type of the template extending this class
 */
public abstract class Template<T extends Template<T>> {
    protected final Writer writer;
    protected       long   written;

    protected Template(Writer writer) {
        this.writer  = writer;
        this.written = 0;
    }

    public Writer getWriter() {
        return writer;
    }

    protected abstract T self();

    /**
     * Renders the template into the owning template's {@code Writer}.
     *
     * @throws Exception if an error occurs during the rendering process
     */
    public abstract void render() throws Exception;

    public T out(String text) {
        try {
            getWriter().write(text);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return self();
    }

    public T outln(String text) {
        try {
            var writer = getWriter();
            writer.write(text);
            writer.write("\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return self();
    }

    public T out(Object... values) {
        try {
            var out = getWriter();
            for (var value : values) {
                out.write(String.valueOf(value));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return self();
    }

    public T outln(Object... values) {
        try {
            var out = getWriter();
            for (var value : values) {
                out.write(String.valueOf(value));
            }
            out.write("\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return self();
    }

    public T outf(String format, Object... args) {
        try {
            getWriter().write(format.formatted(args));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return self();
    }
}
