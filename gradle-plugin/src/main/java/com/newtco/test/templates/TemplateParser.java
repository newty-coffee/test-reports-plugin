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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The TemplateParser class is responsible for parsing template strings into structured parts, which can be either
 * static text, expressions, or code blocks.
 * <p>
 * The parts are identified using regular expressions and organized into a list, making it easier to process and
 * transform template content into executable code.
 */
public class TemplateParser {

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            // Matches:
            //  expressions within <%= %>
            //  import statements
            //  code blocks within <% %>
            "(<%=([^%]*?)%>)" +                         // Group 1: <%= ... %> expressions; Group 2 for the inner content
                    "|" +
                    "(^import\\s+([^;\\r\\n]*);[\\r\\n])" +     // Group 2: Import statements; Group 4 for the inner content
                    "|" +
                    "(<%([^%]*?)%>)",                           // Group 5: <% ... %> code blocks; Group 6 for the inner content
            Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * Parses the given template content string into a list of TemplatePart objects, which represent different parts of
     * the template, including static text, expressions, and code blocks.
     *
     * @param templateContent The content of the template to be parsed.
     * @return A list of TemplatePart objects representing the parsed parts of the template.
     */
    public List<TemplatePart> parse(String templateContent) {
        var parts   = new ArrayList<TemplatePart>();
        var matcher = TOKEN_PATTERN.matcher(templateContent);

        int lastIndex = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end   = matcher.end();

            // Add static text before the token
            if (start > lastIndex) {
                var text = templateContent.substring(lastIndex, start);
                parts.add(new TemplatePart(PartType.TEXT, text));
            }

            if (matcher.group(1) != null) {
                // Expression output within <%= %>
                var expression = matcher.group(2); // Do not trim to preserve formatting
                parts.add(new TemplatePart(PartType.EXPRESSION, expression));
            } else if (matcher.group(3) != null) {
                // import statements
                var expression = matcher.group(4).trim();
                parts.add(new TemplatePart(PartType.IMPORT, expression));
            } else if (matcher.group(5) != null) {
                // Code block within <% %>
                var code = matcher.group(6); // Do not trim to preserve formatting
                parts.add(new TemplatePart(PartType.CODE, code));
            }

            lastIndex = end;
        }

        // Add any remaining static text
        if (lastIndex < templateContent.length()) {
            var text = templateContent.substring(lastIndex);
            parts.add(new TemplatePart(PartType.TEXT, text));
        }

        return parts;
    }

    public enum PartType {
        EXPRESSION,
        IMPORT,
        CODE,
        TEXT,
    }

    public static class TemplatePart {
        public PartType type;
        public String   content;

        public TemplatePart(PartType type, String content) {
            this.type    = type;
            this.content = content;
        }

        public PartType getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return type + ": " + content;
        }
    }
}
