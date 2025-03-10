package dev.langchain4j.microprofile.telemetry;

import java.util.Arrays;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * Creates metrics that follow the
 * <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-spans/">Semantic Conventions
 * for GenAI spans</a>.
 *
 * @author Buhake Sindi
 * @since 25 November 2024
 */
@Dependent
public class SpanChatModelListener implements ChatModelListener {

    private static final String OTEL_SCOPE_KEY_NAME = "OTelScope";
    private static final String OTEL_SPAN_KEY_NAME = "OTelSpan";

    @Inject
    private Tracer tracer;

    /*
     * (non-Javadoc)
     *
     * @see dev.langchain4j.model.chat.listener.ChatModelListener#onRequest(dev.langchain4j.model.chat.listener.
     * ChatModelRequestContext)
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // TODO Auto-generated method stub
        final ChatRequest request = requestContext.chatRequest();
        SpanBuilder spanBuilder = tracer.spanBuilder("chat " + request.parameters().modelName())
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("gen_ai.operation.name", "chat");
        if (request.parameters().maxOutputTokens() != null)
            spanBuilder.setAttribute("gen_ai.request.max_tokens", request.parameters().maxOutputTokens());

        if (request.parameters().temperature() != null)
            spanBuilder.setAttribute("gen_ai.request.temperature", request.parameters().temperature());

        if (request.parameters().topK() != null)
            spanBuilder.setAttribute("gen_ai.request.top_k", request.parameters().topK());

        if (request.parameters().topP() != null)
            spanBuilder.setAttribute("gen_ai.request.top_p", request.parameters().topP());

        if (request.parameters().presencePenalty() != null)
            spanBuilder.setAttribute("gen_ai.request.presence_penalty", request.parameters().presencePenalty());

        if (request.parameters().frequencyPenalty() != null)
            spanBuilder.setAttribute("gen_ai.request.frequency_penalty", request.parameters().frequencyPenalty());

        if (request.parameters().stopSequences() != null)
            spanBuilder.setAttribute(AttributeKey.stringArrayKey("gen_ai.request.stop_sequences"),
                    request.parameters().stopSequences());

        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();

        requestContext.attributes().put(OTEL_SCOPE_KEY_NAME, scope);
        requestContext.attributes().put(OTEL_SPAN_KEY_NAME, span);
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
        Span span = (Span) responseContext.attributes().get(OTEL_SPAN_KEY_NAME);
        if (span != null) {
            ChatResponse response = responseContext.chatResponse();
            span.setAttribute("gen_ai.response.id", response.metadata().id())
                    .setAttribute("gen_ai.response.model", response.metadata().modelName());
            if (response.finishReason() != null) {
                span.setAttribute(AttributeKey.stringArrayKey("gen_ai.response.finish_reasons"),
                        Arrays.asList(response.metadata().finishReason().toString()));
            }
            TokenUsage tokenUsage = response.metadata().tokenUsage();
            if (tokenUsage != null) {
                span.setAttribute("gen_ai.usage.output_tokens", tokenUsage.outputTokenCount())
                        .setAttribute("gen_ai.usage.input_tokens", tokenUsage.inputTokenCount());
            }
            span.end();
        }

        closeScope((Scope) responseContext.attributes().get(OTEL_SCOPE_KEY_NAME));
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
        Span span = (Span) errorContext.attributes().get(OTEL_SPAN_KEY_NAME);
        if (span != null) {
            span.recordException(errorContext.error());
        }

        closeScope((Scope) errorContext.attributes().get(OTEL_SCOPE_KEY_NAME));
    }

    private void closeScope(Scope scope) {
        if (scope != null) {
            scope.close();
        }
    }
}
