package org.eclipse.microprofile.ai.llm;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The value of a method parameter annotated with @UserName will be injected into the field 'name' of a UserMessage.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface UserName {

}
