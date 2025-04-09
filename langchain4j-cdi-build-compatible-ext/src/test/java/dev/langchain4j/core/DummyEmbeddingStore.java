package dev.langchain4j.core;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

@ApplicationScoped
public class DummyEmbeddingStore implements EmbeddingStore<TextSegment> {

    @Override
    public String add(Embedding embedding) {
        return null;
    }

    @Override
    public void add(String string, Embedding embedding) {
    }

    @Override
    public String add(Embedding embedding, TextSegment embd) {
        return null;
    }

    @Override
    public List<String> addAll(List<Embedding> list) {
        return Collections.emptyList();
    }

    @Override
    public List<String> addAll(List<Embedding> list, List<TextSegment> list1) {
        return Collections.emptyList();
    }
}
