package io.smallrye.llm.core;

import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

public class DummyChatLanguageModel implements ChatLanguageModel {
    private String apiKey;
    private EmbeddingModel embeddingModel;

    public DummyChatLanguageModel(String apiKey, EmbeddingModel embeddingModel) {
        this.apiKey = apiKey;
        this.embeddingModel = embeddingModel;
    }

    public String getApiKey() {
        return apiKey;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> list) {
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiKey;
        private EmbeddingModel embeddingModel;

        public DummyChatLanguageModel build() {
            return new DummyChatLanguageModel(this.apiKey, this.embeddingModel);
        }

        public void embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public void apiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
