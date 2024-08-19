package io.smallrye.llm.plugin;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

/*
smallrye.llm.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
smallrye.llm.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
smallrye.llm.plugin.content-retriever.config.api-key=${azure.openai.api.key}
smallrye.llm.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
smallrye.llm.plugin.content-retriever.config.embedding-store=lookup:default
smallrye.llm.plugin.content-retriever.config.embedding-model=lookup:my-model
 */
public class LLMPluginCreator implements SyntheticBeanCreator<Object> {
    @Override
    public Object create(Instance<Object> lookup, Parameters params) {
        return CommonLLMPluginCreator.create(
                lookup,
                params.get(LangChain4JPluginsBuildCompatibleExtension.PARAM_BEANNAME, String.class),
                params.get(LangChain4JPluginsBuildCompatibleExtension.PARAM_TARGET_CLASS, Class.class),
                params.get(LangChain4JPluginsBuildCompatibleExtension.PARAM_BUILDER_CLASS, Class.class));
    }
}
