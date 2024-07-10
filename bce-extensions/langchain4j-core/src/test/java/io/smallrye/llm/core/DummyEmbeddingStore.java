package io.smallrye.llm.core;

import java.util.Collections;
import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingStore;

public class DummyEmbeddingStore implements EmbeddingStore {

    @Override
    public String add(Embedding embedding) {
        return null;
    }

    @Override
    public void add(String string, Embedding embedding) {
    }

    @Override
    public String add(Embedding embedding, Object embd) {
        return null;
    }

    @Override
    public List addAll(List list) {
        return Collections.emptyList();
    }

    @Override
    public List addAll(List list, List list1) {
        return Collections.emptyList();
    }
}
