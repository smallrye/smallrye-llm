package io.smallrye.llm.plugin;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.literal.NamedLiteral;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import io.smallrye.llm.spi.AISyntheticBeanCreatorClassProvider;
 
public class PluginExtension implements BuildCompatibleExtension {
    public static final Logger LOGGER = Logger.getLogger(PluginExtension.class);
    public static final String PREFIX = "smallrye.llm.plugin";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_TARGET_CLASS = "targetClass";
    public static final String PARAM_BUILDER_CLASS = "builderClass";

    @SuppressWarnings("unused")
    @Synthesis
    public void createSynthetics(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        LOGGER.info("CDI BCE Langchain4j plugin");
        Config config = ConfigProvider.getConfig();

        /*
         * smallrye.llm.plugin.content-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
         * smallrye.llm.plugin.content-retriever.scope=jakarta.enterprise.context.ApplicationScoped
         * smallrye.llm.plugin.content-retriever.config.api-key=${azure.openai.api.key}
         * smallrye.llm.plugin.content-retriever.config.endpoint=${azure.openai.endpoint}
         * smallrye.llm.plugin.content-retriever.config.embedding-store.lookup-name=myMemoryEmbeddingStore
         * smallrye.llm.plugin.content-retriever.config.embedding-model.lookup-name=my-model
         */
        // get bean name like content-retiever or "my-model"
        Set<String> beanNameToCreate = getPropertyNameStream(config)
                .filter(prop -> prop.startsWith(PREFIX))
                .map(prop -> prop.substring(PREFIX.length() + 1, prop.indexOf(".", PREFIX.length() + 2)))
                .collect(Collectors.toSet());

        LOGGER.info("detected beans to create : " + beanNameToCreate);

        for (String beanName : beanNameToCreate) {
            prepareBean(config, beanName, syntheticComponents);
        }

    }

    @SuppressWarnings("unchecked")
    private void prepareBean(Config config, String beanName, SyntheticComponents syntheticComponents)
            throws ClassNotFoundException {
        LOGGER.info("Prepare bean " + beanName);

        String newPrefix = PREFIX + "." + beanName;
        String className = config.getValue(newPrefix + ".class", String.class);
        String scopeClassName = config.getOptionalValue(newPrefix + ".scope", String.class).orElse(null);
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
                .withParam(PARAM_PREFIX, newPrefix)
                .withParam(PARAM_TARGET_CLASS, targetClass)
                .withParam(PARAM_BUILDER_CLASS, builderCLass);

        for (Class<?> newInterface : targetClass.getInterfaces())
            builder.type(newInterface);

        LOGGER.info("Types: " + targetClass + ","
                + Arrays.stream(targetClass.getInterfaces()).map(Class::getName).collect(Collectors.joining(",")));

    }

    public static String dashToCamel(String beanName) {
        String fixedBeamName;
        fixedBeamName = Arrays.stream(beanName.split("-"))
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1))
                .collect(Collectors.joining());
        fixedBeamName = fixedBeamName.substring(0, 1).toLowerCase() + fixedBeamName.substring(1);
        return fixedBeamName;
    }

    private static Class<?> loadClass(String scopeClassName) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(scopeClassName);
    }

    public static Stream<String> getPropertyNameStream(Config config) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(config.getPropertyNames().iterator(), Spliterator.ORDERED),
                false);
    }

}
