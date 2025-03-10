package dev.langchain4j.core;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.spi.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(scope = ApplicationScoped.class)
public interface MyDummyApplicationScopedAIService {

}
