package com.newtco.testlib.gradle;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.Project;
import org.mockito.Mockito;

import groovy.lang.MissingPropertyException;

public abstract class MockProject implements Project {
    private       Object              group;
    private       Object              version;
    private       MockProviderFactory providers;
    private final Map<String, Object> properties;

    MockProject() {
        this.properties = new LinkedHashMap<>();
    }

    public static MockProject create() {
        var mock = Mockito.spy(MockProject.class);
        mock.providers = MockProviderFactory.create();
        return mock;
    }


    @Nullable
    @Override
    public Project getParent() {
        return this;
    }

    @Nonnull
    @Override
    public Object getGroup() {
        return group;
    }

    @Override
    public void setGroup(@Nonnull Object group) {
        this.group = group;
    }

    @Nonnull
    @Override
    public Object getVersion() {
        return version;
    }

    @Override
    public void setVersion(@Nonnull Object version) {
        this.version = version;
    }

    @Override
    public void setProperty(@Nonnull String name, @Nullable Object value) throws MissingPropertyException {
        this.properties.put(name, value);
    }

    @Nonnull
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Nullable
    @Override
    public Object property(@Nonnull String propertyName) throws MissingPropertyException {
        if (!properties.containsKey(propertyName)) {
            throw new MissingPropertyException("Property '" + propertyName + "' not found");
        }

        return properties.get(propertyName);
    }

    @Nullable
    @Override
    public Object findProperty(@Nonnull String propertyName) {
        return properties.get(propertyName);
    }

    @Nonnull
    @Override
    public Project getProject() {
        return this;
    }

    @Nonnull
    @Override
    public MockProviderFactory getProviders() {
        return providers;
    }
}
