package dev.langchain4j.cdi.example.booking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@ApplicationScoped
@Path("/car-booking")
public class CarBookingResource {

    @Inject
    private ChatAiService aiService;

    @Inject
    private FraudAiService fraudService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    @Operation(summary = "Chat with an asssitant.", description = "Ask any car booking related question.", operationId = "chatWithAssistant")
    @APIResponse(responseCode = "200", description = "Anwser provided by assistant", content = @Content(mediaType = "text/plain"))
    public String chatWithAssistant(
            @Parameter(description = "The question to ask the assistant", required = true, example = "I want to book a car how can you help me?") @QueryParam("question") String question) {

        String answer;
        try {
            answer = aiService.chat(question);
        } catch (Exception e) {
            e.printStackTrace();
            answer = "My failure reason is:\n\n" + e.getMessage();
        }

        return answer;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/fraud")
    @Operation(summary = "Detect for a customer.", description = "Detect fraud for a customer given his name and surname.", operationId = "detectFraudForCustomer")
    @APIResponse(responseCode = "200", description = "Anwser provided by assistant", content = @Content(mediaType = "application/json"))
    public FraudResponse detectFraudForCustomer(
            @Parameter(description = "Name of the customer to detect fraud for.", required = true, example = "Bond") @QueryParam("name") String name,

            @QueryParam("surname") @Parameter(description = "Surname of the customer to detect fraud for.", required = true, example = "James") String surname) {
        return fraudService.detectFraudForCustomer(name, surname);
    }

}
