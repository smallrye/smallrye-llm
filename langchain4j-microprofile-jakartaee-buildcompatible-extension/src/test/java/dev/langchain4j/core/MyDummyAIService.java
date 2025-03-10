package dev.langchain4j.core;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.spi.RegisterAIService;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService
public interface MyDummyAIService {
    @SystemMessage("sysmsg")
    @UserMessage("usrmsg")
    String detectFraudForCustomer(@V("name") String name, @V("surname") String surname);

    default String fraudFallback(String name, String surname) {
        throw new RuntimeException(
                "Sorry, I am not able to detect fraud for customer " + name + " " + surname
                        + " at the moment. Please try again later.");
    }
}
