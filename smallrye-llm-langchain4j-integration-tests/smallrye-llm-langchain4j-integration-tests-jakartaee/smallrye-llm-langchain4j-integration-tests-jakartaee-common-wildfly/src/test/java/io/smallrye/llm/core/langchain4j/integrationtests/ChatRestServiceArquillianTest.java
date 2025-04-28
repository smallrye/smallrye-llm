package io.smallrye.llm.core.langchain4j.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class ChatRestServiceArquillianTest {

    @SuppressWarnings("unused")
    @Deployment
    public static WebArchive createDeployment() {
        // Include the application classes and the portable extension as a library
        return ShrinkWrap.create(WebArchive.class, "chat-test.war")
                .addClasses(
                        ChatAiService.class,
                        ChatRestService.class,
                        JaxRsApplication.class,
                        DummyLLConfig.class,
                        ChatModelMock.class)
                .addAsLibraries(
                        Maven.resolver()
                                .loadPomFromFile("pom.xml")
                                .resolve(
                                        "io.smallrye.llm:smallrye-llm-langchain4j-portable-extension",
                                        "org.assertj:assertj-core")

                                .withTransitivity()
                                .asFile())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("llm-config.properties")
                .addAsResource("META-INF/services/jakarta.enterprise.inject.spi.Extension")
                .addAsResource("META-INF/services/io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig");
    }

    @SuppressWarnings("unused")
    @ArquillianResource
    private URL baseURL;

    @Test
    public void testChatRestService() {
        String chatEndpoint = baseURL + "chat";
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
