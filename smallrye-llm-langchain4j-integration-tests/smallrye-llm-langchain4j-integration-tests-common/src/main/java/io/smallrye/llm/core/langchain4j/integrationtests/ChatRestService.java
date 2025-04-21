package io.smallrye.llm.core.langchain4j.integrationtests;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatRestService {

    @Inject
    ChatAiService chatAiService;

    @POST
    public String postChat(String chatRequest) {
        return chatAiService.chat(chatRequest);
    }
}
