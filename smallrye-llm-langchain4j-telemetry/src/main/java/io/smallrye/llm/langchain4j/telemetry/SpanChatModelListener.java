package io.smallrye.llm.langchain4j.telemetry;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequest;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponse;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
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
        final ChatModelRequest request = requestContext.request();
        SpanBuilder spanBuilder = tracer.spanBuilder("chat " + request.model())
                .setAttribute("gen_ai.operation.name", "chat");
        if (request.maxTokens() != null)
            spanBuilder.setAttribute("gen_ai.request.max_tokens", request.maxTokens());

        if (request.temperature() != null)
            spanBuilder.setAttribute("gen_ai.request.temperature", request.temperature());

        if (request.topP() != null)
            spanBuilder.setAttribute("gen_ai.request.top_p", request.topP());

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
            ChatModelResponse response = responseContext.response();
            span.setAttribute("gen_ai.response.id", response.id())
                    .setAttribute("gen_ai.response.model", response.model());
            if (response.finishReason() != null) {
                span.setAttribute("gen_ai.response.finish_reasons", response.finishReason().toString());
            }
            TokenUsage tokenUsage = response.tokenUsage();
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
