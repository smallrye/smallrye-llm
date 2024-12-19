package io.smallrye.llm.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Stereotype;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterAIService {

    Class<? extends Annotation> scope() default RequestScoped.class;

    Class<?>[] tools() default {};

    String chatLanguageModelName() default "";

    String contentRetrieverModelName() default "";

    int chatMemoryMaxMessages() default 10;

    String embeddingModelName() default "";

    String embeddingStoreName() default "";

    String contentRetrieverName() default "";

    String moderationModelName() default "";

    String chatMemoryStoreName() default "";
}
