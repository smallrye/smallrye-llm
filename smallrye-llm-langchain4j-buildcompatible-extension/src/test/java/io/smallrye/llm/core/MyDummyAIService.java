package io.smallrye.llm.core;

import org.eclipse.microprofile.ai.llm.RegisterAIService;
import org.eclipse.microprofile.ai.llm.SystemMessage;
import org.eclipse.microprofile.ai.llm.UserMessage;
import org.eclipse.microprofile.ai.llm.V;

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
