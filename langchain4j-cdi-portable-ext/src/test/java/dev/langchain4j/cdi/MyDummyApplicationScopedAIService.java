package dev.langchain4j.cdi;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.microprofile.spi.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(scope = ApplicationScoped.class)
public interface MyDummyApplicationScopedAIService {

}
