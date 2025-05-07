package io.smallrye.llm.core.langchain4j.integrationtests;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

@ApplicationScoped
public class ChatModelMock implements ChatModel {

    private ChatResponse fixedChatResponse;

    public void setFixedChatResponse(ChatResponse fixedChatResponse) {
        this.fixedChatResponse = fixedChatResponse;
    }

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        if (fixedChatResponse != null) {
            return fixedChatResponse;
        }
        return ChatResponse.builder()
                .aiMessage(new AiMessage("ok"))
                .tokenUsage(new TokenUsage(200))
                .build();
    }

    public static ChatModelMockBuilder builder() {
        return new ChatModelMockBuilder();
    }

    public static class ChatModelMockBuilder {

        public ChatModelMock build() {
            return new ChatModelMock();
        }

    }
}