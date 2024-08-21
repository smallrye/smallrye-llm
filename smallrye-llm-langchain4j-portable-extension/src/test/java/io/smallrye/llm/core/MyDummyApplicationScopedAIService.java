package io.smallrye.llm.core;

import jakarta.enterprise.context.ApplicationScoped;

import io.smallrye.llm.spi.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(scope = ApplicationScoped.class)
public interface MyDummyApplicationScopedAIService {

}
