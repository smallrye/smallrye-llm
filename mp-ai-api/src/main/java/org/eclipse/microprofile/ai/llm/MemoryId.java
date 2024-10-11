package org.eclipse.microprofile.ai.llm;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The value of a method parameter annotated with @MemoryId will be used to find the memory belonging to that user/conversation.
 * A parameter annotated with @MemoryId can be of any type, provided it has properly implemented equals() and hashCode()
 * methods.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface MemoryId {

}
