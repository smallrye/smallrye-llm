package io.smallrye.llm.core.langchain4j.integrationtests;

import dev.langchain4j.service.SystemMessage;
import io.smallrye.llm.spi.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(chatModelName = "chat-model")
public interface ChatAiService {

    @SystemMessage("""
            my system message.
            """)
    String chat(String question);

}
