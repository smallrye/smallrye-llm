package io.smallrye.llm.plugin;

import static io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig.PRODUCER;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfigProvider;
import io.smallrye.llm.core.langchain4j.core.config.spi.ProducerFunction;

/*
smallrye.llm.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
smallrye.llm.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
smallrye.llm.plugin.content-retriever.config.api-key=${azure.openai.api.key}
smallrye.llm.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
smallrye.llm.plugin.content-retriever.config.embedding-store=lookup:default
smallrye.llm.plugin.content-retriever.config.embedding-model=lookup:my-model
 */
public class CommonLLMPluginCreator {

    public static final Logger LOGGER = Logger.getLogger(CommonLLMPluginCreator.class);

    @SuppressWarnings("unchecked")
    public static void createAllLLMBeans(LLMConfig llmConfig, Consumer<BeanData> beanBuilder) throws ClassNotFoundException {
        Set<String> beanNameToCreate = llmConfig.getBeanNames();
        LOGGER.info("detected beans to create : " + beanNameToCreate);

        for (String beanName : beanNameToCreate) {
            String className = llmConfig.getBeanPropertyValue(beanName, "class", String.class);
            String scopeClassName = llmConfig.getBeanPropertyValue(beanName, "scope", String.class);
            if (scopeClassName == null || scopeClassName.isBlank()) {
                scopeClassName = ApplicationScoped.class.getName();
            }
            Class<? extends Annotation> scopeClass = (Class<? extends Annotation>) loadClass(scopeClassName);
            Class<?> targetClass = loadClass(className);
            ProducerFunction<Object> producer = llmConfig.getBeanPropertyValue(beanName, PRODUCER,
                    ProducerFunction.class);
            if (producer != null) {
                beanBuilder.accept(
                        new BeanData(targetClass, null, scopeClass, beanName,
                                (Instance<Object> creationalContext) -> producer.produce(creationalContext, beanName)));
            } else {
                // test if there is an inner static class Builder
                Class<?> builderCLass = Arrays.stream(targetClass.getDeclaredClasses())
                        .filter(declClass -> declClass.getName().endsWith("Builder")).findFirst().orElse(null);
                LOGGER.info("Builder class : " + builderCLass);
                if (builderCLass == null) {
                    LOGGER.warn("No builder class found, cancel " + beanName);
                    return;
                }
                beanBuilder.accept(
                        new BeanData(targetClass, builderCLass, scopeClass, beanName,
                                (Instance<Object> creationalContext) -> CommonLLMPluginCreator.create(
                                        creationalContext,
                                        beanName,
                                        targetClass,
                                        builderCLass)));
            }
        }
    }

    public static class BeanData {

        private final Class<?> targetClass;
        private final Class<?> builderClass;
        private final Class<? extends Annotation> scopeClass;
        private final String beanName;
        private final Function<Instance<Object>, Object> callback;

        public BeanData(Class<?> targetClass, Class<?> builderClass, Class<? extends Annotation> scopeClass,
                String beanName, Function<Instance<Object>, Object> callback) {
            this.targetClass = targetClass;
            this.builderClass = builderClass;
            this.scopeClass = scopeClass;
            this.beanName = beanName;
            this.callback = callback;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public Class<?> getBuilderClass() {
            return builderClass;
        }

        public Class<? extends Annotation> getScopeClass() {
            return scopeClass;
        }

        public String getBeanName() {
            return beanName;
        }

        public Function<Instance<Object>, Object> getCallback() {
            return callback;
        }
    }

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
                List<Method> methodsToCall = Arrays.stream(builderClass.getDeclaredMethods())
                        .filter(method -> method.getName().equals(camelCaseProperty))
                        .collect(Collectors.toList());
                if (methodsToCall == null || methodsToCall.isEmpty()) {
                    LOGGER.warn("No method found for " + property + " for bean " + beanName);
                } else {
                    for (Method methodToCall : methodsToCall) {
                        Class<?> parameterType = methodToCall.getParameterTypes()[0];
                        if (stringValue.startsWith("lookup:")) {
                            String lookupableBean = stringValue.substring("lookup:".length());
                            LOGGER.info("Lookup " + lookupableBean + " " + parameterType);
                            Instance<?> inst;
                            if ("default".equals(lookupableBean)) {
                                inst = lookup.select(parameterType);
                                if (!inst.isResolvable()) {
                                    inst = CDI.current().select(parameterType);
                                }
                            } else {
                                inst = lookup.select(parameterType, NamedLiteral.of(lookupableBean));
                            }
                            methodToCall.invoke(builder, inst.get());
                            break;
                        } else {
                            try {
                                Object value = llmConfig.getBeanPropertyValue(beanName, key, parameterType);
                                methodToCall.invoke(builder, value);
                                break;
                            } catch (IllegalArgumentException ex) {
                            }
                        }
                    }
                }
            }
            return builderClass.getMethod("build").invoke(builder);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> loadClass(String scopeClassName) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(scopeClassName);
    }
}
