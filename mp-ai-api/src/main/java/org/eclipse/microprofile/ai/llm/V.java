package org.eclipse.microprofile.ai.llm;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * When a parameter of a method in an AI Service is annotated with {@code @V},
 * it becomes a prompt template variable. Its value will be injected into prompt templates defined
 * via @{@link UserMessage}, @{@link SystemMessage}.
 * <p>
 * Example:
 *
 * <pre>
 * {@code @UserMessage("Hello, my name is {{name}}. I am {{age}} years old.")}
 * String chat(@V("name") String name, @V("age") int age);
 * </pre>
 * <p>
 * Example:
 *
 * <pre>
 * {@code @UserMessage("Hello, my name is {{name}}. I am {{age}} years old.")}
 * String chat(@V String name, @V int age);
 * </pre>
 * <p>
 *
 * @see UserMessage
 * @see SystemMessage
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface V {

    /**
     * Name of a variable (placeholder) in a prompt template.
     */
    String value();
}
