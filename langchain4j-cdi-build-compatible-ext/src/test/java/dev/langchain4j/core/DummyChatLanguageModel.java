package dev.langchain4j.core;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;

public class DummyChatLanguageModel implements ChatLanguageModel {
    private String apiKey;
    private EmbeddingModel embeddingModel;
    private EmbeddingModel embeddingModel2;

    public DummyChatLanguageModel(String apiKey, EmbeddingModel embeddingModel, EmbeddingModel embeddingModel2) {
        this.apiKey = apiKey;
        this.embeddingModel = embeddingModel;
        this.embeddingModel2 = embeddingModel2;
    }

    public String getApiKey() {
        return apiKey;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public EmbeddingModel getEmbeddingModel2() {
        return embeddingModel2;
    }

    @Override
    public ChatResponse chat(List<ChatMessage> list) {
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiKey;
        private EmbeddingModel embeddingModel;
        private EmbeddingModel embeddingModel2;

        public DummyChatLanguageModel build() {
            return new DummyChatLanguageModel(this.apiKey, this.embeddingModel, this.embeddingModel2);
        }

        public void embeddingModel2(EmbeddingModel embeddingModel) {
            this.embeddingModel2 = embeddingModel;
        }

        public void embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public void apiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
