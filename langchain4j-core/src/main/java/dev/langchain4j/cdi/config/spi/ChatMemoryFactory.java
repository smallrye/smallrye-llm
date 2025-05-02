package dev.langchain4j.cdi.config.spi;

import jakarta.enterprise.inject.Instance;

import dev.langchain4j.memory.ChatMemory;

public interface ChatMemoryFactory {

    ChatMemory getChatMemory(Instance<Object> lookup, int size) throws Exception;
}
