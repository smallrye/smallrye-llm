package dev.langchain4j.core;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

@ApplicationScoped
@Named("myModel")
public class DummyEmbeddingModel implements EmbeddingModel {

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> list) {
        return Response.from(Collections.emptyList());
    }

}
