package io.smallrye.llm.langchain4j.metrics;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetadataBuilder;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Tag;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequest;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponse;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import io.smallrye.metrics.SharedMetricRegistries;

/**
 * Creates metrics that follow the
 * <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/">Semantic Conventions
 * for GenAI Metrics</a>.
 *
 * @author Buhake Sindi
 * @since 25 November 2024
 */
@Dependent
public class MetricsChatModelListener implements ChatModelListener {

    private static final String MP_AI_METRIC_START_TIME_NAME = "MP_AI_METRIC_START_TIME";

    private static final String METRIC_CLIENT_TOKEN_USAGE_NAME = "gen_ai.client.token.usage";
    private static final String METRIC_CLIENT_OPERATION_DURATION_NAME = "gen_ai.client.operation.duration";
    //    private static final String METRIC_SERVER_REQUEST_DURATION_NAME = "gen_ai.server.request.duration";
    private static final Tag OPERATION_NAME_TAG = new Tag("gen_ai.operation.name", "chat");

    /*
     * (non-Javadoc)
     *
     * @see dev.langchain4j.model.chat.listener.ChatModelListener#onRequest(dev.langchain4j.model.chat.listener.
     * ChatModelRequestContext)
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // TODO Auto-generated method stub
        requestContext.attributes().put(MP_AI_METRIC_START_TIME_NAME, System.nanoTime());
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.langchain4j.model.chat.listener.ChatModelListener#onResponse(dev.langchain4j.model.chat.listener.
     * ChatModelResponseContext)
     */
    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // TODO Auto-generated method stub
        final long endTime = System.nanoTime();
        final long startTime = (Long) responseContext.attributes().get(MP_AI_METRIC_START_TIME_NAME);

        final ChatModelRequest request = responseContext.request();
        final ChatModelResponse response = responseContext.response();
        Tag requestModelTag = new Tag("gen_ai.request.model", request.model());
        Tag responseModelTag = new Tag("gen_ai.response.model", response.model());

        List<Tag> inputTokenCountTags = List.of(OPERATION_NAME_TAG, requestModelTag, responseModelTag,
                new Tag("gen_ai.token.type", "input"));
        List<Tag> outputTokenCountTags = List.of(OPERATION_NAME_TAG, requestModelTag, responseModelTag,
                new Tag("gen_ai.token.type", "output"));
        List<Tag> durationTags = List.of(OPERATION_NAME_TAG, requestModelTag, responseModelTag);

        recordInputTokenUsage(response.tokenUsage().inputTokenCount(),
                inputTokenCountTags.toArray(new Tag[inputTokenCountTags.size()]));
        recordOutputTokenUsage(response.tokenUsage().outputTokenCount(),
                outputTokenCountTags.toArray(new Tag[inputTokenCountTags.size()]));
        recordDuration(startTime, endTime, durationTags.toArray(new Tag[durationTags.size()]));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.langchain4j.model.chat.listener.ChatModelListener#onError(dev.langchain4j.model.chat.listener.ChatModelErrorContext)
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // TODO Auto-generated method stub
        final long endTime = System.nanoTime();
        final long startTime = (Long) errorContext.attributes().get(MP_AI_METRIC_START_TIME_NAME);
        final ChatModelRequest request = errorContext.request();
        final ChatModelResponse response = errorContext.partialResponse();
        Tag requestModelTag = new Tag("gen_ai.request.model", request.model());
        Tag responseModelTag = new Tag("gen_ai.response.model", response.model());

        StringBuilder sb = new StringBuilder()
                .append(errorContext.error().getClass().getName());

        AiMessage aiMessage = errorContext.partialResponse().aiMessage();
        if (aiMessage != null) {
            sb.append(";" + aiMessage.text());
        }

        Tag errorType = new Tag("error.type", sb.toString());
        List<Tag> tags = List.of(OPERATION_NAME_TAG, requestModelTag, responseModelTag, errorType);
        recordDuration(startTime, endTime, tags.toArray(new Tag[tags.size()]));
    }

    private MetadataBuilder createMetadataBuilder(final String name, final String description) {
        return Metadata.builder()
                .withName(name)
                .withDescription(description);
    }

    private MetadataBuilder createMetadataBuilder(final String name, final String description, final String metricUnit) {
        return createMetadataBuilder(name, description)
                .withUnit(metricUnit);
    }

    private void recordInputTokenUsage(final long tokenCount, final Tag[] tags) {
        Metadata inputTokenUsageMetadata = createMetadataBuilder(METRIC_CLIENT_TOKEN_USAGE_NAME,
                "Measures number of input tokens used").build();
        MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.BASE_SCOPE);
        registry.histogram(inputTokenUsageMetadata, tags).update(tokenCount);
    }

    private void recordOutputTokenUsage(final long tokenCount, final Tag[] tags) {
        Metadata ouputTokenUsageMetadata = createMetadataBuilder(METRIC_CLIENT_TOKEN_USAGE_NAME,
                "Measures number of output tokens used").build();
        MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.BASE_SCOPE);
        registry.histogram(ouputTokenUsageMetadata, tags).update(tokenCount);
    }

    private void recordDuration(final long startTime, long endTime, final Tag[] tags) {
        Metadata durationMetadata = createMetadataBuilder(METRIC_CLIENT_OPERATION_DURATION_NAME, "GenAI operation duration",
                MetricUnits.NANOSECONDS).build();
        MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.BASE_SCOPE);
        registry.timer(durationMetadata, tags).update(Duration.of(endTime - startTime, ChronoUnit.NANOS));
    }
}
