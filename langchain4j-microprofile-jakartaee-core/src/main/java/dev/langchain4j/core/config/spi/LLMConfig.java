package dev.langchain4j.core.config.spi;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/*
dev.langchain4j.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
dev.langchain4j.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
dev.langchain4j.plugin.content-retriever.config.api-key=${azure.openai.api.key}
dev.langchain4j.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
dev.langchain4j.plugin.content-retriever.config.embedding-store=lookup:default
dev.langchain4j.plugin.content-retriever.config.embedding-model=lookup:my-model
 */
public interface LLMConfig {

    String PREFIX = "dev.langchain4j.plugin";
    String PRODUCER = "defined_bean_producer";

    void init();

    /**
     * Get all Smallrye LLM beans names, prefixed by PREFIX
     *
     * @return a set of property names
     */
    Set<String> getBeanNames();

    <T> T getBeanPropertyValue(String beanName, String propertyName, Class<T> type);

    Set<String> getPropertyNamesForBean(String beanName);

    static String getBeanPropertyName(String beanName, String propertyName) {
        return PREFIX + "." + beanName + "." + propertyName;
    }

    static String dashToCamel(String property) {
        String fixed;
        fixed = Arrays.stream(property.split("-"))
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1))
                .collect(Collectors.joining());
        fixed = fixed.substring(0, 1).toLowerCase() + fixed.substring(1);
        return fixed;
    }
}
