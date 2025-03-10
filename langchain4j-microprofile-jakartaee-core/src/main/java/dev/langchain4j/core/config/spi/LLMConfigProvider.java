
package dev.langchain4j.core.config.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class LLMConfigProvider {

    private static LLMConfig llmConfig;
    private static volatile boolean initialized = false;

    static {
        ServiceLoader<LLMConfig> loader = ServiceLoader.load(LLMConfig.class,
                Thread.currentThread().getContextClassLoader());
        final List<LLMConfig> factories = new ArrayList<>();
        loader.forEach(factories::add);
        if (factories.isEmpty()) {
            throw new RuntimeException("No service Found for LLMConfig interface");
        } else {
            llmConfig = factories.iterator().next(); //loader.findFirst().orElse(null);
        }
    }

    public static LLMConfig getLlmConfig() {
        if (!initialized) {
            initialized = true;
            llmConfig.init();
        }

        return llmConfig;
    }
}
