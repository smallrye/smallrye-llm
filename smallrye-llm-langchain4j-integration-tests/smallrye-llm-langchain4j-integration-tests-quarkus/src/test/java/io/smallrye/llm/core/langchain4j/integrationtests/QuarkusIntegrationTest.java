package io.smallrye.llm.core.langchain4j.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QuarkusIntegrationTest {

    @ConfigProperty(name = "quarkus.http.test-port")
    int port;

    @Test
    public void testChatRestService() {
        String chatEndpoint = "http://localhost:" + port + "/chat";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(chatEndpoint);

        String question = "What is the meaning of life?";
        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(question, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
        String result = response.readEntity(String.class);
        assertThat(result).isNotNull().isEqualTo("ok");

        client.close();
    }

}
