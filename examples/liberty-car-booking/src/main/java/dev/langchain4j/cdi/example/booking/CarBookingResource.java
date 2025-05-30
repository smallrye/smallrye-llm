package dev.langchain4j.cdi.example.booking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
    public String chatWithAssistant(
            @QueryParam("question") String question) {

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
    public FraudResponse detectFraudForCustomer(
            @QueryParam("name") String name,
            @QueryParam("surname") String surname) {
        return fraudService.detectFraudForCustomer(name, surname);
    }
}
