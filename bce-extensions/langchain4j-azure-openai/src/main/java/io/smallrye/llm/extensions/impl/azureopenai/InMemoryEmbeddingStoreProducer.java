package io.smallrye.llm.extensions.impl.azureopenai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

/**
 *
 * @author ehugonne
 */

@ApplicationScoped
public class InMemoryEmbeddingStoreProducer {
    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.embedding.store.in-memory.file")
    private String inMemoryEmbeddingStoreFile;

    @ApplicationScoped
    @Produces
    public InMemoryEmbeddingStore buildInMemoryEmbeddingStore() {
        if (inMemoryEmbeddingStoreFile != null) {
            return InMemoryEmbeddingStore.fromFile(inMemoryEmbeddingStoreFile);
        }
        return new InMemoryEmbeddingStore();
    }
}
