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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Utility class to execute git commands from a Gradle project.
 */
public class GitExecutor {

    private GitExecutor() {
    }

    /**
     * Executes a Git command in the context of the given Gradle project.
     *
     * @param project    the Gradle project from which to execute the Git command
     * @param args       the list of arguments for the Git command
     * @param completion the action to perform upon completion of the Git command, receiving the result of the command
     */
    public static void execGit(Project project, List<String> args, Action<GitResult> completion) {
        var log = project.getLogger();

        try (var out = new ByteArrayOutputStream(); var err = new ByteArrayOutputStream()) {

            var execResult = project.exec(spec -> {
                spec.setExecutable("git");
                spec.args(args);
                spec.setStandardOutput(out);
                spec.setErrorOutput(err);
                spec.setIgnoreExitValue(true);
                spec.setWorkingDir(project.getRootDir());
                spec.setEnvironment(Map.of(
                        "LC_ALL", "en_US.UTF-8",
                        "LANG", "en_US.UTF-8"
                ));
            });

            completion.execute(new GitResult(
                    args,
                    normalizeLineEndings(out.toString(StandardCharsets.UTF_8)),
                    err.toString(StandardCharsets.UTF_8),
                    execResult));

        } catch (IOException e) {
            log.error("{}", e.getMessage(), e);
        }
    }

    /**
     * Normalizes line endings by removing '\r' to deal with different line endings returned on Windows and Nix
     * platforms
     */
    private static String normalizeLineEndings(String out) {
        var normalized = new StringBuilder();
        for (char ch : out.toCharArray()) {
            if (ch != '\r') {
                normalized.append(ch);
            }
        }

        return normalized.toString();
    }

    /**
     * Represents the result of executing a Git command. This class encapsulates the output, error, and execution result
     * of the Git command.
     */
    public static class GitResult implements ExecResult {

        private final List<String> args;
        private final String       out;
        private final String       err;
        private final ExecResult   execResult;

        public GitResult(List<String> args, String out, String err, ExecResult execResult) {
            this.args       = args;
            this.out        = out;
            this.err        = err;
            this.execResult = execResult;
        }

        /**
         * Returns the complete Git command by joining the provided arguments.
         *
         * @return The full Git command as a string, prefixed with "git" and including all specified arguments.
         */
        public String getCommand() {
            return "git " + String.join(" ", args);
        }

        /**
         * Returns the output of the Git command.
         *
         * @return The standard output as a string.
         */
        public String getOutput() {
            return out;
        }

        /**
         * Returns the error output of the Git command.
         *
         * @return The error output as a string.
         */
        public String getError() {
            return err;
        }

        @Override
        public int getExitValue() {
            return execResult.getExitValue();
        }

        @Override
        public ExecResult assertNormalExitValue() throws ExecException {
            return execResult.assertNormalExitValue();
        }

        @Override
        public ExecResult rethrowFailure() throws ExecException {
            return execResult.rethrowFailure();
        }
    }
}



