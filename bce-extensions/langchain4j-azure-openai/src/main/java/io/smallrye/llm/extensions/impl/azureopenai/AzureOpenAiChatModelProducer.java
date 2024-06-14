package io.smallrye.llm.extensions.impl.azureopenai;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;

@ApplicationScoped
public class AzureOpenAiChatModelProducer {

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.endpoint")
    private String AZURE_OPENAI_ENDPOINT;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.api.key")
    private String AZURE_OPENAI_KEY;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.deployment.name")
    private String AZURE_OPENAI_DEPLOYMENT_NAME;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.service.version", defaultValue = "2024-02-15-preview")
    private String AZURE_OPENAI_SERVICE_VERSION;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.temperature", defaultValue = "0.1")
    private Double AZURE_OPENAI_TEMPERATURE;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.top.p", defaultValue = "0.1")
    private Double AZURE_OPENAI_TOP_P;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.timeout.seconds", defaultValue = "120")
    private Integer AZURE_OPENAI_TIMEOUT_SECONDS;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.max.retries", defaultValue = "2")
    private Integer AZURE_OPENAI_MAX_RETRIES;

    @Inject
    @ConfigProperty(name = "smallrye.llm.plugin.azure.openai.log.requests.and.responses", defaultValue = "false")
    private Boolean AZURE_OPENAI_LOG_REQUESTS_AND_RESPONSES;

    @ApplicationScoped
    @Produces
    public AzureOpenAiChatModel buildAzureOpenAiChatModel() {
        return AzureOpenAiChatModel.builder()
                .apiKey(AZURE_OPENAI_KEY)
                .endpoint(AZURE_OPENAI_ENDPOINT)
                .serviceVersion(AZURE_OPENAI_SERVICE_VERSION)
                .deploymentName(AZURE_OPENAI_DEPLOYMENT_NAME)
                .temperature(AZURE_OPENAI_TEMPERATURE)
                .topP(AZURE_OPENAI_TOP_P)
                .timeout(Duration.ofSeconds(AZURE_OPENAI_TIMEOUT_SECONDS))
                .maxRetries(AZURE_OPENAI_MAX_RETRIES)
                .logRequestsAndResponses(AZURE_OPENAI_LOG_REQUESTS_AND_RESPONSES)
                .build();
    }
}
