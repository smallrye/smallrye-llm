package io.smallrye.llm.core.langchain4j.services;

import java.util.Map;

import org.eclipse.microprofile.ai.llm.StructuredPrompt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.spi.prompt.structured.StructuredPromptFactory;

/**
 * @author Buhake Sindi
 * @since 10 October 2024
 */
public class SmallRyeStructuredPromptFactory implements StructuredPromptFactory {
	private static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
	
	/* (non-Javadoc)
	 * @see dev.langchain4j.spi.prompt.structured.StructuredPromptFactory#toPrompt(java.lang.Object)
	 */

	@Override
	public Prompt toPrompt(Object structuredPrompt) {
		// TODO Auto-generated method stub
		StructuredPrompt annotation = validateStructuredPrompt(structuredPrompt);

        String promptTemplateString = join(annotation);
        PromptTemplate promptTemplate = PromptTemplate.from(promptTemplateString);

        Map<String, Object> variables = extractVariables(structuredPrompt);

        return promptTemplate.apply(variables);
	}

    /**
     * Extracts the variables from the structured prompt.
     * @param structuredPrompt The structured prompt.
     * @return The variables map.
     */
    private static Map<String, Object> extractVariables(Object structuredPrompt) {
        String json = GSON.toJson(structuredPrompt);
        TypeToken<Map<String, Object>> mapType = new TypeToken<Map<String, Object>>() {};
        return GSON.fromJson(json, mapType);
    }
	
	/**
     * Validates that the given object is annotated with {@link StructuredPrompt}.
     * @param structuredPrompt the object to validate.
     * @return the annotation.
     */
    private static StructuredPrompt validateStructuredPrompt(Object structuredPrompt) {
        ValidationUtils.ensureNotNull(structuredPrompt, "structuredPrompt");

        Class<?> cls = structuredPrompt.getClass();

        return ValidationUtils.ensureNotNull(
                cls.getAnnotation(StructuredPrompt.class),
                "%s should be annotated with @StructuredPrompt to be used as a structured prompt",
                cls.getName());
    }

    /**
     * Joins the lines of the prompt template.
     * @param structuredPrompt the structured prompt.
     * @return the joined prompt template.
     */
    private static String join(StructuredPrompt structuredPrompt) {
        return String.join(structuredPrompt.delimiter(), structuredPrompt.value());
    }
}
