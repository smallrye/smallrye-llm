package io.smallrye.llm.aiservice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.inject.Named;

import org.eclipse.microprofile.ai.llm.RegisterAIService;
import org.eclipse.microprofile.ai.llm.SystemMessage;
import org.eclipse.microprofile.ai.llm.UserMessage;
import org.jboss.logging.Logger;

public class Langchain4JAIServiceBuildCompatibleExtension implements BuildCompatibleExtension {
    private static final Logger LOGGER = Logger.getLogger(Langchain4JAIServiceBuildCompatibleExtension.class);
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

    @Enhancement(types = Object.class, withSubtypes = true)
    @Priority(30)
    public void detectRegisterAIService(ClassConfig config) throws ClassNotFoundException, NoSuchMethodException {
        ClassInfo classInfo = config.info();
        AnnotationInfo annotationInfo = classInfo.annotation(RegisterAIService.class);
        if (annotationInfo != null) {
            registerAIService(config);
        }
    }

    private void registerAIService(ClassConfig classConfig) throws ClassNotFoundException, NoSuchMethodException {
        ClassInfo classInfo = classConfig.info();
        if (classInfo.isInterface()) {
            String className = classInfo.name();
            Class<?> interfaceClass = getLoadClass(className);
            if (!detectedAIServicesDeclaredInterfaces.contains(interfaceClass)) {
                LOGGER.info("RegisterAIService of type " + classInfo.name());
                detectedAIServicesDeclaredInterfaces.add(interfaceClass);
            }

            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
            detectedTools.addAll(Arrays.stream(annotation.tools()).map(Class::getName).collect(Collectors.toList()));
        } else {
            LOGGER.warn("The class is Annotated with @RegisterAIService, but only interface are allowed" + classInfo);
        }
    }

    private void convertSystemMessages(ClassConfig classConfig) throws ClassNotFoundException, NoSuchMethodException {
        AnnotationInfo annotation = classConfig.info().annotation(SystemMessage.class);
        if (annotation != null) {
            classConfig.addAnnotation(SystemMessageAnnotationConverter.fromMP(annotation));
        }
        for (MethodConfig methodConfig : classConfig.methods()) {
            if (methodConfig.info().hasAnnotation(SystemMessage.class)) {
                methodConfig.addAnnotation(
                        SystemMessageAnnotationConverter.fromMP(methodConfig.info().annotation(SystemMessage.class)));
            }
        }
    }

    private void convertUserMessages(ClassConfig classConfig) throws ClassNotFoundException, NoSuchMethodException {
        for (MethodConfig methodConfig : classConfig.methods()) {
            if (methodConfig.info().hasAnnotation(UserMessage.class)) {
                methodConfig.addAnnotation(
                        UserMessageAnnotationConverter.fromMP(methodConfig.info().annotation(UserMessage.class)));
            }
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
