package io.smallrye.llm.core;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import org.jboss.logging.Logger;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.smallrye.llm.spi.RegisterAIService;

public class SmallryeLLMBuildCompatibleExtension implements BuildCompatibleExtension {
    private static final Logger LOGGER = Logger.getLogger(SmallryeLLMBuildCompatibleExtension.class);
    private static final Set<String> detectedAIServicesDeclaredInterfaces = new HashSet<>();
    private static final Set<String> detectedChatLanguageModel = new HashSet<>();
    public static final String PARAM_INTERFACE_CLASS = "interfaceClass";
    public static final String PARAM_DETECTED_CHAT_LANGUAGE_MODEL = "detectedChatLanguageModel";

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

    @SuppressWarnings("unused")
    @Enhancement(types = ChatLanguageModel.class, withSubtypes = true)
    public void detectChatLanguageModel(ClassInfo classInfo) {
        if (!classInfo.name().equals(RegisterAIService.DetectChatLanguageModel.class.getName())) {
            LOGGER.info("Detect new ChatLanguageModel " + classInfo.name());
            detectedChatLanguageModel.add(classInfo.name());
        }
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @Synthesis
    public void synthesis(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        LOGGER.info("Synthesis");

        for (String interfaceName : detectedAIServicesDeclaredInterfaces) {
            LOGGER.info("Create synthetic " + interfaceName);
            Class<?> interfaceClass = Class.forName(interfaceName);
            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);

            Class<? extends ChatLanguageModel> chatLanguageModelClass = annotation.model();
            if (chatLanguageModelClass == RegisterAIService.DetectChatLanguageModel.class) {
                LOGGER.info("Chat language model has to be detected");
                if (detectedChatLanguageModel.isEmpty())
                    throw new IllegalStateException("ChatLanguage model not detected");
                chatLanguageModelClass = (Class<? extends ChatLanguageModel>) Class
                        .forName(detectedChatLanguageModel.iterator().next());
            }
            LOGGER.info("Chat language model fixed to " + chatLanguageModelClass);

            SyntheticBeanBuilder<Object> builder = (SyntheticBeanBuilder<Object>) syntheticComponents.addBean(interfaceClass);

            builder.createWith(AIServiceCreator.class)
                    .type(interfaceClass)
                    .scope(annotation.scope())
                    .withParam(PARAM_INTERFACE_CLASS, interfaceClass)
                    .withParam(PARAM_DETECTED_CHAT_LANGUAGE_MODEL, chatLanguageModelClass);

        }
    }

}
