package io.smallrye.llm.aiservice;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;

import org.jboss.logging.Logger;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import io.smallrye.llm.spi.RegisterAIService;

public class CommonAIServiceCreator {

    private static final Logger LOGGER = Logger.getLogger(CommonAIServiceCreator.class);

    public static <X> X create(Instance<Object> lookup, Class<X> interfaceClass) {
        RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
        Instance<ChatLanguageModel> chatLanguageModel = getInstance(lookup, ChatLanguageModel.class,
                Objects.requireNonNull(annotation).chatLanguageModelName());
        Instance<StreamingChatLanguageModel> streamingChatLanguageModel = getInstance(lookup, StreamingChatLanguageModel.class,
                annotation.streamingChatLanguageModelName());
        Instance<ContentRetriever> contentRetriever = getInstance(lookup, ContentRetriever.class,
                annotation.contentRetrieverName());
        Instance<RetrievalAugmentor> retrievalAugmentor = getInstance(lookup, RetrievalAugmentor.class,
                annotation.retrievalAugmentorName());
        Instance<ToolProvider> toolProvider = getInstance(lookup, ToolProvider.class, annotation.toolProviderName());

        AiServices<X> aiServices = AiServices.builder(interfaceClass);
        if (chatLanguageModel != null && chatLanguageModel.isResolvable()) {
            LOGGER.debug("ChatLanguageModel " + chatLanguageModel.get());
            aiServices.chatLanguageModel(chatLanguageModel.get());
        }
        if (streamingChatLanguageModel != null && streamingChatLanguageModel.isResolvable()) {
            LOGGER.debug("StreamingChatLanguageModel " + streamingChatLanguageModel.get());
            aiServices.streamingChatLanguageModel(streamingChatLanguageModel.get());
        }
        if (contentRetriever != null && contentRetriever.isResolvable()) {
            LOGGER.debug("ContentRetriever " + contentRetriever.get());
            aiServices.contentRetriever(contentRetriever.get());
        }
        if (retrievalAugmentor != null && retrievalAugmentor.isResolvable()) {
            LOGGER.debug("RetrievalAugmentor " + retrievalAugmentor.get());
            aiServices.retrievalAugmentor(retrievalAugmentor.get());
        }
        boolean noToolProvider = true;
        if (toolProvider != null && toolProvider.isResolvable()) {
            LOGGER.debug("ToolProvider " + toolProvider.get());
            aiServices.toolProvider(toolProvider.get());
            noToolProvider = false;
        }
        if (annotation.tools() != null && annotation.tools().length > 0 && noToolProvider) {
            List<Object> tools = new ArrayList<>(annotation.tools().length);
            for (Class<?> toolClass : annotation.tools()) {
                try {
                    tools.add(toolClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error("Can't add toolClass " + toolClass + " for " + interfaceClass + " : " + ex.getMessage(), ex);
                }
            }
            aiServices.tools(tools);
        }
        Instance<ChatMemory> chatMemory = getInstance(lookup, ChatMemory.class,
                annotation.chatMemoryName());
        if (chatMemory != null && chatMemory.isResolvable()) {
            ChatMemory chatMemoryInstance = chatMemory.get();
            LOGGER.debug("ChatMemory " + chatMemoryInstance);
            aiServices.chatMemory(chatMemoryInstance);
        }

        Instance<ChatMemoryProvider> chatMemoryProvider = getInstance(lookup, ChatMemoryProvider.class,
                annotation.chatMemoryProviderName());
        if (chatMemoryProvider != null && chatMemoryProvider.isResolvable()) {
            LOGGER.debug("ChatMemoryProvider " + chatMemoryProvider.get());
            aiServices.chatMemoryProvider(chatMemoryProvider.get());
        }

        Instance<ModerationModel> moderationModelInstance = getInstance(lookup, ModerationModel.class,
                annotation.moderationModelName());
        if (moderationModelInstance != null && moderationModelInstance.isResolvable()) {
            LOGGER.debug("ModerationModel " + moderationModelInstance.get());
            aiServices.moderationModel(moderationModelInstance.get());
        }

        return aiServices.build();
    }

    private static <X> Instance<X> getInstance(Instance<Object> lookup, Class<X> type, String name) {
        LOGGER.debug("CDI get instance of type '" + type + "' with name '" + name + "'");
        if (name != null && !name.isBlank()) {
            if ("#default".equals(name))
                return lookup.select(type);

            return lookup.select(type, NamedLiteral.of(name));
        }

        return null;
    }
}
