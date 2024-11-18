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

package com.newtco.test.test;

import java.util.List;
import java.util.Map;

public class Main {

    public String test1() {
        return "test1";
    }

    public List<Object> test2() {
        return List.of("test2", 2, 2.0f, true, Map.of("test2", "test2"));
    }

    public String test3() {
        return """
                test1
                test2
                test3
                """;
    }

    public String test4() {
        // NO COVERAGE
        return null;
    }

    public interface TestCallback<T> {
        void callback(T value);
    }

    public static class NoCoverage {
        public void test1() {
            System.out.println("test1");
        }

        public void test2() {
            System.out.println("test2");
        }
    }
}
