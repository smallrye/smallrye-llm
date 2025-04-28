
package io.smallrye.llm.core.langchain4j.core.config.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

public class LLMConfigProvider {

    private static LLMConfig llmConfig;
    private static volatile boolean initialized = false;
    private static final Logger LOGGER = Logger.getLogger(LLMConfigProvider.class);

    static {
        ServiceLoader<LLMConfig> loader = ServiceLoader.load(LLMConfig.class,
                Thread.currentThread().getContextClassLoader());
        final List<LLMConfig> factories = new ArrayList<>();
        loader.forEach(factories::add);
        if (factories.isEmpty()) {
            throw new RuntimeException("No service Found for LLMConfig interface");
        } else {
            llmConfig = factories.iterator().next(); //loader.findFirst().orElse(null);
            LOGGER.debug("Found LLMConfig interface: " + llmConfig.getClass().getName());
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
