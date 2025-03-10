package dev.langchain4j.microprofile.telemetry;

import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponse;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;

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

    private LongHistogram clientTokenUsage;
    private DoubleHistogram clientOperationDuration;

    @Inject
    private Meter meter;

    @PostConstruct
    private void init() {
        clientTokenUsage = meter.histogramBuilder(METRIC_CLIENT_TOKEN_USAGE_NAME)
                .ofLongs()
                .setDescription("Measures number of input and output tokens used")
                .setExplicitBucketBoundariesAdvice(List.of(1L, 4L, 16L, 64L, 256L, 1024L, 4096L, 16384L, 65536L, 262144L,
                        1048576L, 4194304L, 16777216L, 67108864L))
                .build();

        clientOperationDuration = meter.histogramBuilder(METRIC_CLIENT_OPERATION_DURATION_NAME)
                .setDescription("GenAI operation duration")
                .setExplicitBucketBoundariesAdvice(
                        List.of(0.01, 0.02, 0.04, 0.08, 0.16, 0.32, 0.64, 1.28, 2.56, 5.12, 10.24, 20.48, 40.96, 81.92))
                .setUnit("s")
                .build();
    }

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        requestContext.attributes().put(MP_AI_METRIC_START_TIME_NAME, System.nanoTime());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        final long endTime = System.nanoTime();
        final long startTime = (Long) responseContext.attributes().get(MP_AI_METRIC_START_TIME_NAME);

        final ChatRequest request = responseContext.chatRequest();
        final ChatResponse response = responseContext.chatResponse();

        Attributes inputTokenCountAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.parameters().modelName(),
                AttributeKey.stringKey("gen_ai.response.model"), response.metadata().modelName(),
                AttributeKey.stringKey("gen_ai.token.type"), "input");
        //Record
        clientTokenUsage.record(response.tokenUsage().inputTokenCount(), inputTokenCountAttributes);

        Attributes outputTokenCountAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.parameters().modelName(),
                AttributeKey.stringKey("gen_ai.response.model"), response.metadata().modelName(),
                AttributeKey.stringKey("gen_ai.token.type"), "output");

        //Record
        clientTokenUsage.record(response.tokenUsage().outputTokenCount(), outputTokenCountAttributes);

        //Record duration
        Attributes durationAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.parameters().modelName(),
                AttributeKey.stringKey("gen_ai.response.model"), response.metadata().modelName());
        recordClientOperationDuration(startTime, endTime, durationAttributes);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.langchain4j.model.chat.listener.ChatModelListener#onError(dev.langchain4j.model.chat.listener.ChatModelErrorContext)
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        final long endTime = System.nanoTime();
        final long startTime = (Long) errorContext.attributes().get(MP_AI_METRIC_START_TIME_NAME);
        final ChatRequest request = errorContext.chatRequest();
        final ChatModelResponse response = errorContext.partialResponse();

        StringBuilder sb = new StringBuilder()
                .append(errorContext.error().getClass().getName());

        AiMessage aiMessage = errorContext.partialResponse().aiMessage();
        if (aiMessage != null) {
            sb.append(";").append(aiMessage.text());
        }

        //Record duration
        Attributes durationAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.parameters().modelName(),
                AttributeKey.stringKey("gen_ai.response.model"), response.model(),
                AttributeKey.stringKey("error.type"), sb.toString());
        recordClientOperationDuration(startTime, endTime, durationAttributes);
    }

    private void recordClientOperationDuration(final long startTime, long endTime, final Attributes attributes) {
        clientOperationDuration.record(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS), attributes);
    }
}
