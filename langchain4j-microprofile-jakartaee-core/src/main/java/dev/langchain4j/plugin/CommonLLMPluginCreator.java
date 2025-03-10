package dev.langchain4j.plugin;

import static dev.langchain4j.core.config.spi.LLMConfig.PRODUCER;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.logging.Logger;

import dev.langchain4j.core.config.spi.LLMConfig;
import dev.langchain4j.core.config.spi.LLMConfigProvider;
import dev.langchain4j.core.config.spi.ProducerFunction;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.store.embedding.EmbeddingStore;

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

    private static final Map<Class<?>, TypeLiteral<?>> TYPE_LITERALS = new HashMap<>();

    static {
        TYPE_LITERALS.put(EmbeddingStore.class, new TypeLiteral<EmbeddingStore<TextSegment>>() {
        });
    }

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
                                (Instance<Object> creationalContext) -> {
                                    return CommonLLMPluginCreator.create(
                                            creationalContext,
                                            beanName,
                                            targetClass,
                                            builderCLass);
                                }));
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

    @SuppressWarnings("unchecked")
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
                        if ("listeners".equals(property)) {
                            Class<?> typeParameterClass = ChatLanguageModel.class.isAssignableFrom(targetClass)
                                    || StreamingChatLanguageModel.class.isAssignableFrom(targetClass)
                                            ? ChatModelListener.class
                                            : parameterType.getTypeParameters()[0].getGenericDeclaration();
                            List<Object> listeners = (List<Object>) Collections.checkedList(new ArrayList<>(),
                                    typeParameterClass);
                            if ("@all".equals(stringValue.trim())) {
                                Instance<Object> inst = (Instance<Object>) getInstance(lookup, typeParameterClass);
                                if (inst != null) {
                                    inst.forEach(listeners::add);
                                }
                            } else {
                                try {
                                    for (String className : stringValue.split(",")) {
                                        Instance<?> inst = getInstance(lookup, loadClass(className.trim()));
                                        listeners.add(inst.get());
                                    }
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (listeners != null && !listeners.isEmpty()) {
                                listeners.stream().forEach(l -> LOGGER.info("Adding listener: " + l.getClass().getName()));
                                methodToCall.invoke(builder, listeners);
                            }
                        } else if (stringValue.startsWith("lookup:")) {
                            String lookupableBean = stringValue.substring("lookup:".length());
                            LOGGER.info("Lookup " + lookupableBean + " " + parameterType);
                            Instance<?> inst;
                            if ("default".equals(lookupableBean)) {
                                inst = getInstance(lookup, parameterType);
                            } else {
                                inst = getInstance(lookup, parameterType, lookupableBean);
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

    private static Class<?> loadClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    @SuppressWarnings("unchecked")
    private static <T> Instance<T> getInstance(Instance<Object> lookup, Class<T> clazz) {
        if (TYPE_LITERALS.containsKey(clazz))
            return (Instance<T>) lookup.select(TYPE_LITERALS.get(clazz));
        return lookup.select(clazz);
    }

    private static <T> Instance<T> getInstance(Instance<Object> lookup, Class<T> clazz, String lookupName) {
        if (lookupName == null || lookupName.isBlank())
            return getInstance(lookup, clazz);
        return lookup.select(clazz, NamedLiteral.of(lookupName));
    }
}
