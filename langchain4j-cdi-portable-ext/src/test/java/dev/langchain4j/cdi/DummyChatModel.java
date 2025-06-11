package dev.langchain4j.cdi;

import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

public class DummyChatModel implements ChatModel {
    private String apiKey;
    private EmbeddingModel embeddingModel;
    private EmbeddingModel embeddingModel2;

    public DummyChatModel(String apiKey, EmbeddingModel embeddingModel, EmbeddingModel embeddingModel2) {
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

    public Response<AiMessage> generate(List<ChatMessage> list) {
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiKey;
        private EmbeddingModel embeddingModel;
        private EmbeddingModel embeddingModel2;

        public DummyChatModel build() {
            return new DummyChatModel(this.apiKey, this.embeddingModel, this.embeddingModel2);
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
