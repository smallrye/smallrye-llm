package io.smallrye.llm.core.langchain4j.integrationtests;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;

public class DummyLLConfig implements LLMConfig {
    Properties properties = new Properties();
    private static final Logger LOGGER = Logger.getLogger(DummyLLConfig.class);

    @Override
    public void init() {
        LOGGER.info("Initializing Dummy LLConfig");
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("llm-config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
        properties.keySet().forEach(key -> {
            LOGGER.info("Key: " + key);
        });
    }

    @Override
    public Set<String> getBeanNames() {
        return properties.keySet().stream().map(Object::toString)
                .filter(prop -> prop.startsWith(PREFIX))
                .map(prop -> prop.substring(PREFIX.length() + 1, prop.indexOf(".", PREFIX.length() + 2)))
                .collect(Collectors.toSet());
    }

    @Override
    public <T> T getBeanPropertyValue(String beanName, String propertyName, Class<T> type) {
        String value = properties.getProperty(PREFIX + "." + beanName + "." + propertyName);
        if (value == null)
            return null;
        if (type == String.class)
            return (T) value;
        if (type == Duration.class)
            return (T) Duration.parse(value);
        try {
            return type.getConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Set<String> getPropertyNamesForBean(String beanName) {
        String configPrefix = PREFIX + "." + beanName + ".config.";
        return properties.keySet().stream().map(Object::toString)
                .filter(prop -> prop.startsWith(configPrefix))
                .map(prop -> prop.substring(configPrefix.length()))
                .collect(Collectors.toSet());
    }
}
