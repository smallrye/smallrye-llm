package dev.langchain4j.core.spi.portableextension;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.logging.Logger;

import dev.langchain4j.core.config.spi.LLMConfig;
import dev.langchain4j.core.config.spi.LLMConfigProvider;
import dev.langchain4j.plugin.CommonLLMPluginCreator;

public class LangChain4JPluginsPortableExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(LangChain4JPluginsPortableExtension.class);
    private LLMConfig llmConfig;

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
            throws ClassNotFoundException {
        if (llmConfig == null) {
            llmConfig = LLMConfigProvider.getLlmConfig();
        }

        CommonLLMPluginCreator.createAllLLMBeans(
                llmConfig,
                beanData -> {
                    LOGGER.debug("Add Bean " + beanData.getTargetClass() + " " + beanData.getScopeClass() + " "
                            + beanData.getBeanName());

                    afterBeanDiscovery.addBean()
                            .types(beanData.getTargetClass())
                            .addTypes(beanData.getTargetClass().getInterfaces())
                            .scope(beanData.getScopeClass())
                            .name(beanData.getBeanName())
                            .qualifiers(NamedLiteral.of(beanData.getBeanName()))
                            .produceWith(beanData.getCallback());

                    LOGGER.info("Types: " + beanData.getTargetClass() + ","
                            + Arrays.stream(beanData.getTargetClass().getInterfaces()).map(Class::getName)
                                    .collect(Collectors.joining(",")));

                });
    }

}
