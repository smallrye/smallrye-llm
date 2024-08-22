package io.smallrye.llm.core.langchain4j.portableextension;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.logging.Logger;

import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfigProvider;
import io.smallrye.llm.plugin.CommonLLMPluginCreator;

public class LangChain4JPluginsPortableExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(LangChain4JPluginsPortableExtension.class);
    private LLMConfig llmConfig;

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
            throws ClassNotFoundException {
        if (llmConfig == null)
            llmConfig = LLMConfigProvider.getLlmConfig();

        CommonLLMPluginCreator.createAllLLMBeans(
                llmConfig,
                beanData -> {
                    LOGGER.info("Add Bean " + beanData.getTargetClass() + " " + beanData.getScopeClass() + " "
                            + beanData.getBeanName());

                    afterBeanDiscovery.addBean()
                            .types(beanData.getTargetClass())
                            .addTypes(beanData.getTargetClass().getInterfaces())
                            .scope(beanData.getScopeClass())
                            .name(beanData.getBeanName())
                            .qualifiers(NamedLiteral.of(beanData.getBeanName()))
                            .produceWith(creationalContext -> CommonLLMPluginCreator.create(
                                    creationalContext,
                                    beanData.getBeanName(),
                                    beanData.getTargetClass(),
                                    beanData.getBuilderClass()));

                    LOGGER.info("Types: " + beanData.getTargetClass() + ","
                            + Arrays.stream(beanData.getTargetClass().getInterfaces()).map(Class::getName)
                                    .collect(Collectors.joining(",")));

                });
    }

}
