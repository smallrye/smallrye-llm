package dev.langchain4j.cdi.spi;

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

    String chatModelName() default "#default";

    String streamingChatModelName() default "";

    String contentRetrieverName() default "";

    String moderationModelName() default "";

    String chatMemoryName() default "";

    String chatMemoryProviderName() default "";

    String retrievalAugmentorName() default "";

    String toolProviderName() default "";

}