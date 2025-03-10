package dev.langchain4j.plugin;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.literal.NamedLiteral;

import org.jboss.logging.Logger;

import dev.langchain4j.core.config.spi.LLMConfig;
import dev.langchain4j.core.config.spi.LLMConfigProvider;
import dev.langchain4j.spi.AISyntheticBeanCreatorClassProvider;

public class LangChain4JPluginsBuildCompatibleExtension implements BuildCompatibleExtension {
    public static final Logger LOGGER = Logger.getLogger(LangChain4JPluginsBuildCompatibleExtension.class);
    public static final String PARAM_BEANNAME = "beanName";
    public static final String PARAM_TARGET_CLASS = "targetClass";
    public static final String PARAM_BUILDER_CLASS = "builderClass";

    private LLMConfig llmConfig;

    @SuppressWarnings({ "unchecked" })
    @Synthesis
    public void createSynthetics(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        if (llmConfig == null) {
            llmConfig = LLMConfigProvider.getLlmConfig();
        }
        LOGGER.info("CDI BCE Langchain4j plugin");

        /*
         * dev.langchain4j.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
         * dev.langchain4j.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
         * dev.langchain4j.plugin.content-retriever.config.api-key=${azure.openai.api.key}
         * dev.langchain4j.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
         * dev.langchain4j.plugin.content-retriever.config.embedding-store.lookup-name=myMemoryEmbeddingStore
         * dev.langchain4j.plugin.content-retriever.config.embedding-model.lookup-name=my-model
         */
        // get bean name like content-retiever or "my-model"

        CommonLLMPluginCreator.createAllLLMBeans(
                llmConfig,
                beanData -> {
                    SyntheticBeanBuilder<Object> builder = (SyntheticBeanBuilder<Object>) syntheticComponents
                            .addBean(beanData.getTargetClass());

                    builder.createWith(AISyntheticBeanCreatorClassProvider.getSyntheticBeanCreatorClass())
                            .type(beanData.getTargetClass())
                            .scope(beanData.getScopeClass())
                            .name(beanData.getBeanName())
                            .qualifier(NamedLiteral.of(beanData.getBeanName()))
                            .withParam(PARAM_BEANNAME, beanData.getBeanName())
                            .withParam(PARAM_TARGET_CLASS, beanData.getTargetClass())
                            .withParam(PARAM_BUILDER_CLASS, beanData.getBuilderClass());

                    for (Class<?> newInterface : beanData.getTargetClass().getInterfaces())
                        builder.type(newInterface);
                });
    }
}
