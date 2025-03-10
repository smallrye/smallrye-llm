package dev.langchain4j.core.config.spi;

import java.util.Optional;
import java.util.ServiceLoader;

import jakarta.enterprise.inject.Instance;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;

public class ChatMemoryFactoryProvider {
    private static final ChatMemoryFactory factory;
    static {
        ServiceLoader<ChatMemoryFactory> loader = ServiceLoader.load(ChatMemoryFactory.class,
                ChatMemoryFactoryProvider.class.getClassLoader());
        Optional<ChatMemoryFactory> instance = loader.findFirst();
        if (instance.isEmpty()) {
            factory = (Instance<Object> lookup, int size) -> MessageWindowChatMemory.withMaxMessages(size);
        } else {
            factory = instance.get();
        }
    }

    /**
     * @return the ChatMemoryFactory.
     */
    public static ChatMemoryFactory getChatMemoryFactory() {
        return factory;
    }
}
