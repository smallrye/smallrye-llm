package io.smallrye.llm.langchain4jadapter;

import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MPDefaultAiServices;
import dev.langchain4j.spi.services.AiServicesFactory;

public class MPAIServiceFactory implements AiServicesFactory {
    @Override
    public <T> AiServices<T> create(AiServiceContext context) {
        return new MPDefaultAiServices<T>(context);
    }
}
