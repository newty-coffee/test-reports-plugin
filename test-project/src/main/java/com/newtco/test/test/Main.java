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

public class Main {

    public void test1() {
        System.out.println("test1");
    }

    public void test2() {
        System.out.println("test2");
    }

    public void test3(boolean flag) {
        if (flag) {
            print(new TestCallback<String>() {

                @Override
                public void call1() {
                    System.out.println("test3:call1");
                }

                @Override
                public void call2(String value) {
                    System.out.println("test3:call2:" + value);
                }
            });
        }
        else {
            System.out.println("test3");
        }
    }

    private void print(TestCallback<String>output) {
        output.call1();
        output.call2("call2 output");
    }

    abstract static class TestCallback<T> {
        abstract void call1();

        abstract void call2(T value);
    }
}
