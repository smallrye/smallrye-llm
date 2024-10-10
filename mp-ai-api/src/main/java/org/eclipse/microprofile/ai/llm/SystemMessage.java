package org.eclipse.microprofile.ai.llm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemMessage {
    String[] value() default { "" };

    String delimiter() default "\n";

    String fromResource() default "";
}
