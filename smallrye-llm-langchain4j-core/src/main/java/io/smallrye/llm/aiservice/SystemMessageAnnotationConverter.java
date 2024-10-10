package io.smallrye.llm.aiservice;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.ai.llm.SystemMessage;

public class SystemMessageAnnotationConverter {

    public static SystemMessage fromMP(AnnotationInfo source) {
        return new LangChain4JSystemMessage(source);
    }

    public static class LangChain4JSystemMessage extends AnnotationLiteral<SystemMessage> implements SystemMessage {
        private final AnnotationInfo source;

        public LangChain4JSystemMessage(AnnotationInfo source) {
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
