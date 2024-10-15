package io.smallrye.llm.core.langchain4j.services;

import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.spi.services.AiServicesFactory;

/**
 * @author Buhake Sindi
 * @since 10 October 2024
 */
public class SmallRyeLang4JchainAiServicesFactory implements AiServicesFactory {

    /*
     * (non-Javadoc)
     *
     * @see dev.langchain4j.spi.services.AiServicesFactory#create(dev.langchain4j.service.AiServiceContext)
     */
    @Override
    public <T> AiServices<T> create(AiServiceContext context) {
        // TODO Auto-generated method stub
        return new SmallRyeLang4JAiServices<T>(context);
    }
}
