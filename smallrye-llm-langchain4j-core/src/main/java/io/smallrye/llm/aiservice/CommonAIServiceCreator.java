package io.smallrye.llm.aiservice;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import io.smallrye.llm.spi.RegisterAIService;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;

public class CommonAIServiceCreator {

    private static final Logger LOGGER = Logger.getLogger(CommonAIServiceCreator.class);

    public static <X> X create(Instance<Object> lookup, Class<X> interfaceClass) {
        RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
        Instance<ChatLanguageModel> chatLanguageModel = getInstance(lookup, ChatLanguageModel.class,
                annotation.chatLanguageModelName());
        Instance<StreamingChatLanguageModel> streamingChatLanguageModel = getInstance(lookup, StreamingChatLanguageModel.class,
                annotation.streamingChatLanguageModelName());
        Instance<ContentRetriever> contentRetriever = getInstance(lookup, ContentRetriever.class,
                annotation.contentRetrieverName());
        Instance<RetrievalAugmentor> retrievalAugmentor = getInstance(lookup, RetrievalAugmentor.class,
                annotation.retrievalAugmentorName());
        try {
            AiServices<X> aiServices = AiServices.builder(interfaceClass);
            if (chatLanguageModel != null && chatLanguageModel.isResolvable()) {
                LOGGER.info("ChatLanguageModel " + chatLanguageModel.get());
                aiServices.chatLanguageModel(chatLanguageModel.get());
            }
            if (streamingChatLanguageModel != null && streamingChatLanguageModel.isResolvable()) {
                LOGGER.info("StreamingChatLanguageModel " + streamingChatLanguageModel.get());
                aiServices.streamingChatLanguageModel(streamingChatLanguageModel.get());
            }
            if (contentRetriever != null && contentRetriever.isResolvable()) {
                LOGGER.info("ContentRetriever " + contentRetriever.get());
                aiServices.contentRetriever(contentRetriever.get());
            }
            if (retrievalAugmentor != null && retrievalAugmentor.isResolvable()) {
                LOGGER.info("RetrievalAugmentor " + retrievalAugmentor.get());
                aiServices.retrievalAugmentor(retrievalAugmentor.get());
            }
            if (annotation.tools() != null && annotation.tools().length > 0) {
                List<Object> tools = new ArrayList<>(annotation.tools().length);
                for (Class toolClass : annotation.tools()) {
                    try {
                        tools.add(toolClass.getConstructor((Class<?>[])null).newInstance((Object[])null));
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException ex) {
                    }
                }
                aiServices.tools(tools);
            }
            
            Instance<ChatMemory> chatMemory = getInstance(lookup, ChatMemory.class,
                    annotation.chatMemoryName());
            if (chatMemory != null && chatMemory.isResolvable()) {
                LOGGER.info("ChatMemory " + chatMemory.get());
                aiServices.chatMemory(chatMemory.get());
            } 

            Instance<ChatMemoryProvider> chatMemoryProvider = getInstance(lookup, ChatMemoryProvider.class,
            		annotation.chatMemoryProviderName());
            if (chatMemoryProvider != null && chatMemoryProvider.isResolvable()) {
            	LOGGER.info("ChatMemoryProvider " + chatMemoryProvider.get());
                aiServices.chatMemoryProvider(chatMemoryProvider.get());
            } 

            Instance<ModerationModel> moderationModelInstance = getInstance(lookup, ModerationModel.class,
            		annotation.moderationModelName());
            if (moderationModelInstance != null && moderationModelInstance.isResolvable()) {
            	LOGGER.info("ModerationModel " + moderationModelInstance.get());
                aiServices.moderationModel(moderationModelInstance.get());
            }
        
            return aiServices.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <X> Instance<X> getInstance(Instance<Object> lookup, Class<X> type, String name) {
        LOGGER.info("CDI get instance of type '" + type + "' with name '" + name + "'");
        if (name != null && !name.isBlank()) {
        	if ("#default".equals(name)) 
        		return lookup.select(type);
        	
        	return lookup.select(type, NamedLiteral.of(name));
        }
        
        return null;
    }
}
