package io.smallrye.llm.core;

import java.util.List;

import jakarta.enterprise.context.Dependent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

@Dependent
public class DummyChatLanguageModel implements ChatLanguageModel {
    @Override
    public Response<AiMessage> generate(List<ChatMessage> list) {
        return null;
    }
}
