package io.smallrye.llm.aiservice;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.ai.llm.UserMessage;

public class UserMessageAnnotationConverter {

    public static UserMessage fromMP(AnnotationInfo source) {
        return new LangChain4JUserMessage(source);
    }

    public static class LangChain4JUserMessage extends AnnotationLiteral<UserMessage> implements UserMessage {
        private final AnnotationInfo source;

        public LangChain4JUserMessage(AnnotationInfo source) {
            this.source = source;
        }

        @Override
        public String[] value() {
            return source.value().asArray().stream().map(AnnotationMember::asString).toArray(String[]::new);
        }

        @Override
        public String delimiter() {
            return source.member("delimiter").asString();
        }

        @Override
        public String fromResource() {
            return source.member("fromResource").asString();
        }
    }
}
