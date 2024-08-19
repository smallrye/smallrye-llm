package io.smallrye.llm.core.langchain4j.portableextension;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
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

        Set<String> beanNameToCreate = llmConfig.getBeanNames();

        LOGGER.info("detected beans to create : " + beanNameToCreate);

        for (String beanName : beanNameToCreate) {
            prepareBean(beanName, afterBeanDiscovery);
        }
    }

    @SuppressWarnings("unchecked")
    private void prepareBean(String beanName, AfterBeanDiscovery afterBeanDiscovery)
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

        LOGGER.info("Add Bean " + targetClass + " " + scopeClass + " " + beanName);

        afterBeanDiscovery.addBean()
                .types(targetClass)
                .addTypes(targetClass.getInterfaces())
                .scope(scopeClass)
                .name(beanName)
                .qualifiers(NamedLiteral.of(beanName))
                .createWith(
                        creationalContext -> CommonLLMPluginCreator.create(CDI.current(), beanName, targetClass, builderCLass));

        LOGGER.info("Types: " + targetClass + ","
                + Arrays.stream(targetClass.getInterfaces()).map(Class::getName).collect(Collectors.joining(",")));
    }

    private static Class<?> loadClass(String scopeClassName) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(scopeClassName);
    }

}
