package com.newtco.testlib.gradle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.provider.Provider;
import org.mockito.Mockito;

public abstract class MockProviderFactory implements org.gradle.api.provider.ProviderFactory {
    private Map<String, String> gradleProperties;
    private Map<String, String> systemProperties;
    private Map<String, String> environmentVariables;

    MockProviderFactory() {
    }

    public static MockProviderFactory create() {
        return create(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public static MockProviderFactory create(Map<String, String> gradleProperties, Map<String, String> systemProperties, Map<String, String> environmentVariables) {
        var mock = Mockito.spy(MockProviderFactory.class);
        mock.gradleProperties     = gradleProperties;
        mock.systemProperties     = systemProperties;
        mock.environmentVariables = environmentVariables;
        return mock;
    }

    @Nonnull
    @Override
    public Provider<String> environmentVariable(@Nonnull String variableName) {
        return mockProvider(variableName, environmentVariables);
    }

    @Nonnull
    @Override
    public Provider<String> systemProperty(@Nonnull String propertyName) {
        return mockProvider(propertyName, systemProperties);
    }

    @Nonnull
    @Override
    public Provider<String> gradleProperty(@Nonnull String propertyName) {
        return mockProvider(propertyName, gradleProperties);
    }

    // For testing
    public MockProviderFactory setGradlePropertyForTest(String name, String value) {
        gradleProperties.put(name, value);
        return this;
    }

    public MockProviderFactory setSystemPropertyForTest(String name, String value) {
        systemProperties.put(name, value);
        return this;
    }

    public MockProviderFactory setEnvironmentVariableForTest(String name, String value) {
        environmentVariables.put(name, value);
        return this;
    }

    private Provider<String> mockProvider(String name, Map<String, String> map) {
        var mock = MockProvider.<String>create(() -> map.get(name), () -> map.containsKey(name));
        return mock;
    }

    public static abstract class MockProvider<T> implements Provider<T> {
        private Supplier<T>       getValue;
        private Supplier<Boolean> isPresent;

        MockProvider() {
        }

        // Create a missing value provider
        public static <T> MockProvider<T> create() {
            var mock = Mockito.<MockProvider<T>>spy();
            mock.getValue  = () -> null;
            mock.isPresent = () -> false;
            return mock;
        }

        public static <T> MockProvider<T> create(T value) {
            var mock = Mockito.<MockProvider<T>>spy();
            mock.getValue  = () -> value;
            mock.isPresent = () -> value != null;
            return mock;
        }


        public static <T> MockProvider<T> create(Supplier<T> getValue, Supplier<Boolean> isPresent) {
            var mock = Mockito.<MockProvider<T>>spy();
            mock.getValue  = getValue;
            mock.isPresent = isPresent;
            return mock;
        }

        @Nonnull
        @Override
        public T get() {
            if (!isPresent()) {
                throw new IllegalStateException("Property is missing");
            }
            return getValue.get();
        }

        @Nullable
        @Override
        public T getOrNull() {
            return isPresent() ? getValue.get() : null;
        }

        @Nonnull
        @Override
        public T getOrElse(@Nonnull T defaultValue) {
            return isPresent() ? getValue.get() : defaultValue;
        }

        @Override
        public boolean isPresent() {
            return this.isPresent.get();
        }
    }
}
