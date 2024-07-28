package io.smallrye.llm.core;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;

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

        CDI<Object> cdi = CDI.current();
        ChatLanguageModel chatLanguageModel = getChatLanguageModel(annotation);
        ContentRetriever contentRetriever = getContentRetriever(annotation);
        try {
            AiServices<?> aiServices = AiServices.builder(interfaceClass)
                    .chatLanguageModel(chatLanguageModel)
                    .tools(Stream.of(annotation.tools())
                            .map(c -> cdi.select(c).get())
                            .collect(Collectors.toList()))
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(annotation.chatMemoryMaxMessages()));
            if (contentRetriever != null)
                aiServices.contentRetriever(contentRetriever);

            Instance<ContentRetriever> contentRetrievers = cdi.select(ContentRetriever.class);
            if (contentRetrievers.isResolvable())
                aiServices.contentRetriever(contentRetrievers.get());

            return aiServices.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ChatLanguageModel getChatLanguageModel(RegisterAIService annotation) {
        if (annotation.chatLanguageModelName().isBlank())
            return CDI.current().select(ChatLanguageModel.class).get();
        return CDI.current().select(ChatLanguageModel.class, NamedLiteral.of(annotation.chatLanguageModelName())).get();
    }

    private static ContentRetriever getContentRetriever(RegisterAIService annotation) {
        if (annotation.contentRetrieverModelName().isBlank()) {
            Instance<ContentRetriever> contentRetrievers = CDI.current().select(ContentRetriever.class);
            if (contentRetrievers.isResolvable())
                return contentRetrievers.get();
        }

        Instance<ContentRetriever> contentRetrievers = CDI.current().select(ContentRetriever.class,
                NamedLiteral.of(annotation.contentRetrieverModelName()));
        if (contentRetrievers.isResolvable())
            return contentRetrievers.get();
        return null;
    }
}
