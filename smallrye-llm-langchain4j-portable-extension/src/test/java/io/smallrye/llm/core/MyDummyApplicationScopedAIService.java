package io.smallrye.llm.core;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.ai.llm.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(scope = ApplicationScoped.class)
public interface MyDummyApplicationScopedAIService {

}
