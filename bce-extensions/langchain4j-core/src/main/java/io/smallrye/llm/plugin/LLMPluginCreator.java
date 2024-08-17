package io.smallrye.llm.plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.literal.NamedLiteral;
import java.lang.reflect.InvocationTargetException;


import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/*
smallrye.llm.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
smallrye.llm.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
smallrye.llm.plugin.content-retriever.config.api-key=${azure.openai.api.key}
smallrye.llm.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
smallrye.llm.plugin.content-retriever.config.embedding-store=lookup:default
smallrye.llm.plugin.content-retriever.config.embedding-model=lookup:my-model
 */
public class LLMPluginCreator implements SyntheticBeanCreator<Object> {
    @SuppressWarnings("DataFlowIssue")
    @Override
    public Object create(Instance<Object> lookup, Parameters params) {
        String prefix = params.get(PluginExtension.PARAM_PREFIX, String.class);
        Class<?> targetClass = params.get(PluginExtension.PARAM_TARGET_CLASS, Class.class);
        Class<?> builderClass = params.get(PluginExtension.PARAM_BUILDER_CLASS, Class.class);

        PluginExtension.LOGGER.info(
                "Create instance config:" + prefix + ", target class : " + targetClass + ", builderClass : " + builderClass);

        String configPrefix = prefix + ".config.";
        try {
            Object builder = targetClass.getMethod("builder").invoke(null);

            Config config = ConfigProvider.getConfig();
            Set<String> properties = PluginExtension.getPropertyNameStream(config)
                    .filter(prop -> prop.startsWith(configPrefix))
                    .map(prop -> prop.substring(configPrefix.length()))
                    .collect(Collectors.toSet());

            for (String property : properties) {
                String camelCaseProperty = PluginExtension.dashToCamel(property);
                String stringValue = config.getValue(configPrefix + property, String.class);
                PluginExtension.LOGGER
                        .info("Attempt to feed : " + property + " (" + camelCaseProperty + ") with : " + stringValue);
                Method methodToCall = Arrays.stream(builderClass.getDeclaredMethods())
                        .filter(method -> method.getName().equals(camelCaseProperty))
                        .findFirst().orElse(null);
                Class<?> parameterType = methodToCall.getParameterTypes()[0];
                if (stringValue.startsWith("lookup:")) {
                    String beanName = stringValue.substring("lookup:".length());
                    PluginExtension.LOGGER.info("Lookup " + beanName + " " + parameterType);
                    Instance<?> inst;
                    if ("default".equals(beanName)) {
                        inst = lookup.select(parameterType);
                    } else {
                        inst = lookup.select(parameterType, NamedLiteral.of(beanName));
                    }
                    methodToCall.invoke(builder, inst.get());
                } else {
                    Object value = config.getValues(configPrefix + property, parameterType).get(0);
                    methodToCall.invoke(builder, value);
                }
                // get Method
            }
            return builderClass.getMethod("build").invoke(builder);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
