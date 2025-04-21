package io.smallrye.llm.core.langchain4j.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;

@HelidonTest
public class HelidonIntegrationTest {

    @Inject
    WebTarget injectedTarget;

    @Test
    public void testChatRestService() {
        WebTarget target = injectedTarget.path("/chat");

        String question = "What is the meaning of life?";
        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(question, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
        String result = response.readEntity(String.class);
        assertThat(result).isNotNull().isEqualTo("ok");
    }

}
