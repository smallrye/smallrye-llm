package io.smallrye.llm.plugin;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.literal.NamedLiteral;

import org.jboss.logging.Logger;

import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfigProvider;
import io.smallrye.llm.spi.AISyntheticBeanCreatorClassProvider;

public class LangChain4JPluginsBuildCompatibleExtension implements BuildCompatibleExtension {
    public static final Logger LOGGER = Logger.getLogger(LangChain4JPluginsBuildCompatibleExtension.class);
    public static final String PARAM_BEANNAME = "beanName";
    public static final String PARAM_TARGET_CLASS = "targetClass";
    public static final String PARAM_BUILDER_CLASS = "builderClass";

    private LLMConfig llmConfig;

    @SuppressWarnings("unused")
    @Synthesis
    public void createSynthetics(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        if (llmConfig == null)
            llmConfig = LLMConfigProvider.getLlmConfig();
        LOGGER.info("CDI BCE Langchain4j plugin");

        /*
         * smallrye.llm.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
         * smallrye.llm.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
         * smallrye.llm.plugin.content-retriever.config.api-key=${azure.openai.api.key}
         * smallrye.llm.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
         * smallrye.llm.plugin.content-retriever.config.embedding-store.lookup-name=myMemoryEmbeddingStore
         * smallrye.llm.plugin.content-retriever.config.embedding-model.lookup-name=my-model
         */
        // get bean name like content-retiever or "my-model"
        Set<String> beanNameToCreate = llmConfig.getBeanNames();

        LOGGER.info("detected beans to create : " + beanNameToCreate);

        for (String beanName : beanNameToCreate) {
            prepareBean(beanName, syntheticComponents);
        }

    }

    @SuppressWarnings("unchecked")
    private void prepareBean(String beanName, SyntheticComponents syntheticComponents)
            throws ClassNotFoundException {
        LOGGER.info("Prepare bean " + beanName);

        String className = llmConfig.getBeanPropertyValue(beanName, "class", String.class);
        String scopeClassName = llmConfig.getBeanPropertyValue(beanName, "scope", String.class);
        if (scopeClassName == null || scopeClassName.isBlank())
            scopeClassName = ApplicationScoped.class.getName();
        Class<? extends Annotation> scopeClass = (Class<? extends Annotation>) loadClass(scopeClassName);
        Class<?> targetClass = loadClass(className);

        // test if there is an inneer static class Builder
        Class<?> builderCLass = Arrays.stream(targetClass.getDeclaredClasses())
                .filter(declClass -> declClass.getName().endsWith("Builder")).findFirst().orElse(null);
        LOGGER.info("Builder class : " + builderCLass);
        if (builderCLass == null) {
            LOGGER.warn("No builder class found, cancel " + beanName);
            return;
        }

        LOGGER.info("bean name: " + beanName);

        SyntheticBeanBuilder<Object> builder = (SyntheticBeanBuilder<Object>) syntheticComponents.addBean(targetClass);

        builder.createWith(AISyntheticBeanCreatorClassProvider.getSyntheticBeanCreatorClass())
                .type(targetClass)
                .scope(scopeClass)
                .name(beanName)
                .qualifier(NamedLiteral.of(beanName))
                .withParam(PARAM_BEANNAME, beanName)
                .withParam(PARAM_TARGET_CLASS, targetClass)
                .withParam(PARAM_BUILDER_CLASS, builderCLass);

        for (Class<?> newInterface : targetClass.getInterfaces())
            builder.type(newInterface);

        LOGGER.info("Types: " + targetClass + ","
                + Arrays.stream(targetClass.getInterfaces()).map(Class::getName).collect(Collectors.joining(",")));
    }

    private static Class<?> loadClass(String scopeClassName) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(scopeClassName);
    }

}
