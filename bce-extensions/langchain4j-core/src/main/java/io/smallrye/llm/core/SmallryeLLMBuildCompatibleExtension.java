package io.smallrye.llm.core;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.*;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import org.jboss.logging.Logger;

import io.smallrye.llm.spi.RegisterAIService;

public class SmallryeLLMBuildCompatibleExtension implements BuildCompatibleExtension {
    private static final Logger LOGGER = Logger.getLogger(SmallryeLLMBuildCompatibleExtension.class);
    private static final Set<String> detectedAIServicesDeclaredInterfaces = new HashSet<>();
    public static final String PARAM_INTERFACE_CLASS = "interfaceClass";

    public static Set<String> getDetectedAIServicesDeclaredInterfaces() {
        return detectedAIServicesDeclaredInterfaces;
    }

    @SuppressWarnings("unused")
    @Discovery
    public void registerCDIComponents(ScannedClasses scannedClasses) {
        LOGGER.info("Core ext");
    }

    @SuppressWarnings("unused")
    @Enhancement(types = Object.class, withAnnotations = RegisterAIService.class, withSubtypes = true)
    public void detectRegisterAIService(ClassInfo classInfo) {
        // ajout opentrace
        LOGGER.info("Detect new AIService " + classInfo.name());
        detectedAIServicesDeclaredInterfaces.add(classInfo.name());
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @Synthesis
    public void synthesis(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        LOGGER.info("Synthesis");

        for (String interfaceName : detectedAIServicesDeclaredInterfaces) {
            LOGGER.info("Create synthetic " + interfaceName);
            Class<?> interfaceClass = Class.forName(interfaceName);
            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);

            SyntheticBeanBuilder<Object> builder = (SyntheticBeanBuilder<Object>) syntheticComponents.addBean(interfaceClass);

            builder.createWith(AIServiceCreator.class)
                    .type(interfaceClass)
                    .scope(annotation.scope())
                    .withParam(PARAM_INTERFACE_CLASS, interfaceClass);

        }
    }

}
