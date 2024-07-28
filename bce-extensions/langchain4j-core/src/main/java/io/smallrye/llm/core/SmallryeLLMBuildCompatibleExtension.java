package io.smallrye.llm.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.inject.Named;

import org.jboss.logging.Logger;

import io.smallrye.llm.spi.RegisterAIService;

public class SmallryeLLMBuildCompatibleExtension implements BuildCompatibleExtension {
    private static final Logger LOGGER = Logger.getLogger(SmallryeLLMBuildCompatibleExtension.class);
    private static final Set<Class<?>> detectedAIServicesDeclaredInterfaces = new HashSet<>();
    private static final Set<String> detectedTools = new HashSet<>();
    public static final String PARAM_INTERFACE_CLASS = "interfaceClass";

    public static Set<Class<?>> getDetectedAIServicesDeclaredInterfaces() {
        return detectedAIServicesDeclaredInterfaces;
    }

    @SuppressWarnings("unused")
    @Enhancement(types = Object.class, withSubtypes = true)
    @Priority(20)
    public void protectedToolsToBePurgedByQuarkus(ClassConfig classConfig) throws ClassNotFoundException {
        if (detectedTools.contains(classConfig.info().name())) {
            Class<?> toolClass = getLoadClass(classConfig.info().name());
            if (toolClass.getAnnotation(Named.class) == null) {
                classConfig.addAnnotation(NamedLiteral.of("quarkus-protected-" + classConfig.info().name()));
                LOGGER.info("Add a Name to " + classConfig.info().name());
            }
        }
    }

    @SuppressWarnings("unused")
    @Enhancement(types = Object.class, withAnnotations = RegisterAIService.class, withSubtypes = true)
    @Priority(10)
    public void detectRegisterAIService(ClassConfig classConfig) throws ClassNotFoundException {
        ClassInfo classInfo = classConfig.info();
        String className = classInfo.name();
        LOGGER.info("Analyze from Ehancement " + className);
        if (classInfo.isInterface()) {
            Class<?> interfaceClass = getLoadClass(className);
            detectedAIServicesDeclaredInterfaces.add(interfaceClass);
            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
            detectedTools.addAll(Arrays.stream(annotation.tools()).map(Class::getName).collect(Collectors.toList()));
            LOGGER.info("Current detected tools : " + detectedTools);
        } else {
            LOGGER.warn("The class is Annotated with @RegisterAIService, but only interface are allowed" + classConfig.info());
        }
    }

    private static Class<?> getLoadClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @Synthesis
    public void synthesisAllRegisterAIServices(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        LOGGER.info("synthesisAllRegisterAIServices");

        for (Class<?> interfaceClass : detectedAIServicesDeclaredInterfaces) {
            LOGGER.info("Create synthetic " + interfaceClass);
            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);

            detectedTools.addAll(Arrays.stream(annotation.tools()).map(Class::getName).collect(Collectors.toSet()));

            SyntheticBeanBuilder<Object> builder = (SyntheticBeanBuilder<Object>) syntheticComponents.addBean(interfaceClass);

            builder.createWith(AIServiceCreator.class)
                    .type(interfaceClass)
                    .scope(annotation.scope())
                    .name("registeredAIService-" + interfaceClass.getName())
                    .withParam(PARAM_INTERFACE_CLASS, interfaceClass);

        }
    }

}
