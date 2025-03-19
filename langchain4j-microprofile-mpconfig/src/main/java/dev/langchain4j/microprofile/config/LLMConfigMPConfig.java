package dev.langchain4j.microprofile.config;

import static dev.langchain4j.core.config.spi.LLMConfig.getBeanPropertyName;

import java.util.Collections;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import dev.langchain4j.core.config.spi.LLMConfig;

public class LLMConfigMPConfig implements LLMConfig {

    private Set<String> beanNames;
    private Config config;

    @Override
    public void init() {
        config = ConfigProvider.getConfig();
        beanNames = getPropertyNameStream(config)
                .filter(prop -> prop.startsWith(PREFIX))
                .map(prop -> prop.substring(PREFIX.length() + 1, prop.indexOf(".", PREFIX.length() + 2)))
                .collect(Collectors.toSet());
    }

    private static Stream<String> getPropertyNameStream(Config config) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(config.getPropertyNames().iterator(), Spliterator.ORDERED),
                false);
    }

    @Override
    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(beanNames);
    }

    @Override
    public <T> T getBeanPropertyValue(String beanName, String propertyName, Class<T> type) {
        if (PRODUCER.equals(propertyName)) {
            return null;
        }
        T value = config.getOptionalValue(getBeanPropertyName(beanName, propertyName), type).orElse(null);
        return value;
    }

    @Override
    public Set<String> getPropertyNamesForBean(String beanName) {
        String configPrefix = PREFIX + "." + beanName + ".config.";
        return getPropertyNameStream(config)
                .filter(prop -> prop.startsWith(configPrefix))
                .map(prop -> prop.substring(configPrefix.length()))
                .collect(Collectors.toSet());
    }
}
