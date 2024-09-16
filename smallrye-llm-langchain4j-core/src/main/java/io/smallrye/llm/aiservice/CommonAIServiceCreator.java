package io.smallrye.llm.aiservice;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;

import org.jboss.logging.Logger;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import io.smallrye.llm.core.langchain4j.core.config.spi.ChatMemoryFactoryProvider;
import io.smallrye.llm.spi.RegisterAIService;

public class CommonAIServiceCreator {

    private static final Logger LOGGER = Logger.getLogger(CommonAIServiceCreator.class);

    @SuppressWarnings("unchecked")
    public static Object create(Instance<Object> lookup, Class<?> interfaceClass) {
        RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
        Instance<ChatLanguageModel> chatLanguageModel = getInstance(lookup, ChatLanguageModel.class,
                annotation.chatLanguageModelName());
        Instance<ContentRetriever> contentRetriever = getInstance(lookup, ContentRetriever.class,
                annotation.contentRetrieverName());
        try {
            AiServices<?> aiServices = AiServices.builder(interfaceClass);
            if (chatLanguageModel.isResolvable()) {
                LOGGER.info("ChatLanguageModel " + chatLanguageModel.get());
                aiServices.chatLanguageModel(chatLanguageModel.get());
            }
            if (contentRetriever.isResolvable()) {
                LOGGER.info("ContentRetriever " + contentRetriever.get());
                aiServices.contentRetriever(contentRetriever.get());
            }
            if (annotation.tools() != null && annotation.tools().length > 0) {
                List<Object> tools = new ArrayList<>(annotation.tools().length);
                for (Class toolClass : annotation.tools()) {
                    try {
                        tools.add(toolClass.getConstructor(null).newInstance(null));
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException ex) {
                    }
                }
                aiServices.tools(tools);
            }
            aiServices.chatMemory(
                    ChatMemoryFactoryProvider.getChatMemoryFactory().getChatMemory(lookup, annotation.chatMemoryMaxMessages()));
            return aiServices.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Instance getInstance(Instance<Object> lookup, Class<?> type, String name) {
        LOGGER.info("Getinstance of '" + type + "' with name '" + name + "'");
        if (name == null || name.isBlank()) {
            return lookup.select(type);
        }
        return lookup.select(type, NamedLiteral.of(name));
    }

}
