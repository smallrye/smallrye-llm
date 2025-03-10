package io.jefrajames.booking;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import dev.langchain4j.core.config.spi.LLMConfig;

public class DummyLLConfig implements LLMConfig {
    Properties properties = new Properties();

    @Override
    public void init() {
        try (FileReader fileReader = new FileReader(System.getProperty("llmconfigfile"))) {
            properties.load(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getBeanNames() {
        return properties.keySet().stream().map(Object::toString)
                .filter(prop -> prop.startsWith(PREFIX))
                .map(prop -> prop.substring(PREFIX.length() + 1, prop.indexOf(".", PREFIX.length() + 2)))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
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
            throw new RuntimeException(e);
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
