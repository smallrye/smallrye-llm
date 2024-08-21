package io.smallrye.llm.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;

import org.jboss.logging.Logger;

import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfigProvider;

/*
smallrye.llm.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
smallrye.llm.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
smallrye.llm.plugin.content-retriever.config.api-key=${azure.openai.api.key}
smallrye.llm.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
smallrye.llm.plugin.content-retriever.config.embedding-store=lookup:default
smallrye.llm.plugin.content-retriever.config.embedding-model=lookup:my-model
 */
public class CommonLLMPluginCreator {
    @SuppressWarnings("DataFlowIssue")
    public static final Logger LOGGER = Logger.getLogger(CommonLLMPluginCreator.class);

    public static Object create(Instance<Object> lookup, String beanName, Class<?> targetClass, Class<?> builderClass) {
        LLMConfig llmConfig = LLMConfigProvider.getLlmConfig();
        LOGGER.info(
                "Create instance config:" + beanName + ", target class : " + targetClass + ", builderClass : " + builderClass);
        try {
            Object builder = targetClass.getMethod("builder").invoke(null);
            Set<String> properties = llmConfig.getPropertyNamesForBean(beanName);
            for (String property : properties) {
                String camelCaseProperty = LLMConfig.dashToCamel(property);
                LOGGER.info("Bean " + beanName + " " + property);
                String key = "config." + property;
                String stringValue = llmConfig.getBeanPropertyValue(beanName, key, String.class);
                LOGGER.info("Attempt to feed : " + property + " (" + camelCaseProperty + ") with : " + stringValue);
                Method methodToCall = Arrays.stream(builderClass.getDeclaredMethods())
                        .filter(method -> method.getName().equals(camelCaseProperty))
                        .findFirst().orElse(null);
                Class<?> parameterType = methodToCall.getParameterTypes()[0];
                if (stringValue.startsWith("lookup:")) {
                    String lookupableBean = stringValue.substring("lookup:".length());
                    LOGGER.info("Lookup " + lookupableBean + " " + parameterType);
                    Instance<?> inst;
                    if ("default".equals(lookupableBean)) {
                        inst = lookup.select(parameterType);
                    } else {
                        inst = lookup.select(parameterType, NamedLiteral.of(lookupableBean));
                    }
                    methodToCall.invoke(builder, inst.get());
                } else {
                    Object value = llmConfig.getBeanPropertyValue(beanName, key, parameterType);
                    methodToCall.invoke(builder, value);
                }
            }
            return builderClass.getMethod("build").invoke(builder);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
