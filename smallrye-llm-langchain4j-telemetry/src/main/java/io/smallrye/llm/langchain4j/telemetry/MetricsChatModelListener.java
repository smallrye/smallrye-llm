package io.smallrye.llm.langchain4j.telemetry;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequest;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponse;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
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

    private LongHistogram inputTokenUsage;
    private LongHistogram outputTokenUsage;
    private DoubleHistogram duration;

    @Inject
    private Meter meter;

    @PostConstruct
    private void init() {
        inputTokenUsage = meter.histogramBuilder(METRIC_CLIENT_TOKEN_USAGE_NAME)
                .ofLongs()
                .setDescription("Measures number of input tokens used")
                .build();

        outputTokenUsage = meter.histogramBuilder(METRIC_CLIENT_TOKEN_USAGE_NAME)
                .ofLongs()
                .setDescription("Measures number of output tokens used")
                .build();

        duration = meter.histogramBuilder(METRIC_CLIENT_OPERATION_DURATION_NAME)
                .setDescription("GenAI operation duration")
                .setUnit("s")
                .build();
    }

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

        Attributes inputTokenCountAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.model(),
                AttributeKey.stringKey("gen_ai.response.model"), response.model(),
                AttributeKey.stringKey("gen_ai.token.type"), "input");
        //Record
        inputTokenUsage.record(response.tokenUsage().inputTokenCount(), inputTokenCountAttributes);

        Attributes outputTokenCountAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.model(),
                AttributeKey.stringKey("gen_ai.response.model"), response.model(),
                AttributeKey.stringKey("gen_ai.token.type"), "output");

        //Record
        outputTokenUsage.record(response.tokenUsage().outputTokenCount(), outputTokenCountAttributes);

        //Record duration
        Attributes durationAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.model(),
                AttributeKey.stringKey("gen_ai.response.model"), response.model());
        recordDuration(startTime, endTime, durationAttributes);
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

        StringBuilder sb = new StringBuilder()
                .append(errorContext.error().getClass().getName());

        AiMessage aiMessage = errorContext.partialResponse().aiMessage();
        if (aiMessage != null) {
            sb.append(";" + aiMessage.text());
        }

        //Record duration
        Attributes durationAttributes = Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat",
                AttributeKey.stringKey("gen_ai.request.model"), request.model(),
                AttributeKey.stringKey("gen_ai.response.model"), response.model(),
                AttributeKey.stringKey("error.type"), sb.toString());
        recordDuration(startTime, endTime, durationAttributes);
    }

    private void recordDuration(final long startTime, long endTime, final Attributes attributes) {
        duration.record(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS), attributes);
    }
}
