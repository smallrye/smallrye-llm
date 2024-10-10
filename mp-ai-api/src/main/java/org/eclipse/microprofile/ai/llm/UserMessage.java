package org.eclipse.microprofile.ai.llm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface UserMessage {
    String[] value() default { "" };

    String delimiter() default "\n";

    String fromResource() default "";
}
