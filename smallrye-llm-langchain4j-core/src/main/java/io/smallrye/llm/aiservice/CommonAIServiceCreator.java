package io.smallrye.llm.aiservice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Moderate;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.smallrye.llm.core.langchain4j.core.config.spi.ChatMemoryFactoryProvider;
import io.smallrye.llm.spi.RegisterAIService;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;

public class CommonAIServiceCreator {

	private static final Logger LOGGER = Logger.getLogger(CommonAIServiceCreator.class);

    @SuppressWarnings("unchecked")
    public static <X> X create(Instance<Object> lookup, Class<X> interfaceClass) {
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
                for (Class<?> toolClass : annotation.tools()) {
                    try {
                        tools.add(toolClass.getConstructor(null).newInstance(null));
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException ex) {
                    }
                }
                aiServices.tools(tools);
            }
            
            ChatMemoryProvider chatMemoryProvider = createChatMemoryProvider(lookup, interfaceClass, annotation);
            if (chatMemoryProvider != null) {
            	aiServices.chatMemoryProvider(chatMemoryProvider);
            } else {
            	aiServices.chatMemory(
                    ChatMemoryFactoryProvider.getChatMemoryFactory().getChatMemory(lookup, annotation.chatMemoryMaxMessages()));
            }
            
            ModerationModel moderationModel = findModerationModel(lookup, interfaceClass, annotation);
            if (moderationModel != null) {
            	aiServices.moderationModel(moderationModel);
            }
            return (X)aiServices.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <X> Instance<X> getInstance(Instance<Object> lookup, Class<X> type, String name) {
        LOGGER.info("Getinstance of '" + type + "' with name '" + name + "'");
        if (name == null || name.isBlank()) {
            return lookup.select(type);
        }
        return lookup.select(type, NamedLiteral.of(name));
    }
    
    private static ModerationModel findModerationModel(Instance<Object> lookup, Class<?> interfaceClass, RegisterAIService registerAIService) {
    	//Get all methods.
    	for (Method method : interfaceClass.getMethods()) {
    		Moderate moderate = method.getAnnotation(Moderate.class);
    		if (moderate != null) {
    			Instance<ModerationModel> moderationModelInstance = getInstance(lookup, ModerationModel.class, registerAIService.moderationModelName());
    			if (moderationModelInstance != null && moderationModelInstance.isResolvable()) return moderationModelInstance.get();
    		}
    	}
    	
    	return null;
    }
    
    
    private static ChatMemoryProvider createChatMemoryProvider(Instance<Object> lookup, Class<?> interfaceClass, RegisterAIService registerAIService) {
    	//Get all methods.
    	for (Method method : interfaceClass.getMethods()) {
    		for (Parameter parameter : method.getParameters()) {
    			MemoryId memoryIdAnnotation = parameter.getAnnotation(MemoryId.class);
    			if (memoryIdAnnotation != null) {
    				Instance<ChatMemoryStore> chatMemoryStore = getInstance(lookup, ChatMemoryStore.class,
    						registerAIService.chatMemoryStoreName());
    				if (chatMemoryStore == null || !chatMemoryStore.isResolvable()) {
    					throw new IllegalStateException("Unable to resolve a ChatMemoryStore for your ChatMemoryProvider.");
    				}
    				
    				ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
    		                .id(memoryId)
    		                .maxMessages(registerAIService.chatMemoryMaxMessages())
    		                .chatMemoryStore(chatMemoryStore.get())
    		                .build();
    				return chatMemoryProvider;
    			}
    		}
    	}
    	
    	return null;
    }
}
