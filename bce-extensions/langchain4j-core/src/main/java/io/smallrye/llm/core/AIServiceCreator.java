package io.smallrye.llm.core;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.literal.NamedLiteral;
import org.jboss.logging.Logger;


import org.jboss.logging.Logger;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import io.smallrye.llm.spi.RegisterAIService;

public class AIServiceCreator implements SyntheticBeanCreator<Object> {
    private static final Logger LOGGER = Logger.getLogger(AIServiceCreator.class);

    @Override
    public Object create(Instance<Object> lookup, Parameters params) {
        Class<?> interfaceClass = params.get(SmallryeLLMBuildCompatibleExtension.PARAM_INTERFACE_CLASS, Class.class);
        RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
        ChatLanguageModel chatLanguageModel = getChatLanguageModel(lookup, annotation);
        ContentRetriever contentRetriever = getContentRetriever(lookup, annotation);
        try {
            AiServices<?> aiServices = AiServices.builder(interfaceClass)
                    .chatLanguageModel(chatLanguageModel)
                    .tools(Stream.of(annotation.tools())
                            .map(c -> lookup.select(c).get())
                            .collect(Collectors.toList()))
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(annotation.chatMemoryMaxMessages()));
            if (contentRetriever != null)
                aiServices.contentRetriever(contentRetriever);

            Instance<ContentRetriever> contentRetrievers = lookup.select(ContentRetriever.class);
            if (contentRetrievers.isResolvable())
                aiServices.contentRetriever(contentRetrievers.get());

            return aiServices.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ChatLanguageModel getChatLanguageModel(Instance<Object> lookup,RegisterAIService annotation) {
        if (annotation.chatLanguageModelName().isBlank())
            return lookup.select(ChatLanguageModel.class).get();
        return lookup.select(ChatLanguageModel.class, NamedLiteral.of(annotation.chatLanguageModelName())).get();
    }

    private static ContentRetriever getContentRetriever(Instance<Object> lookup,RegisterAIService annotation) {
        if (annotation.contentRetrieverModelName().isBlank()) {
            Instance<ContentRetriever> contentRetrievers = lookup.select(ContentRetriever.class);

            if (contentRetrievers.isResolvable())
                return contentRetrievers.get();
        }

        Instance<ContentRetriever> contentRetrievers = lookup.select(ContentRetriever.class,
                NamedLiteral.of(annotation.contentRetrieverModelName()));
        if (contentRetrievers.isResolvable())
            return contentRetrievers.get();
        return null;
    }
}
