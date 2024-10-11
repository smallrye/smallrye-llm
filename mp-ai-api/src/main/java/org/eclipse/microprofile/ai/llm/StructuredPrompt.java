package org.eclipse.microprofile.ai.llm;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Represents a structured prompt.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface StructuredPrompt {

    /**
     * Prompt template can be defined in one line or multiple lines.
     * If the template is defined in multiple lines, the lines will be joined with a delimiter defined below.
     *
     * @return the prompt template lines.
     */
    String[] value();

    /**
     * The delimiter to join the lines of the prompt template.
     *
     * @return the delimiter.
     */
    String delimiter() default "\n";
}
