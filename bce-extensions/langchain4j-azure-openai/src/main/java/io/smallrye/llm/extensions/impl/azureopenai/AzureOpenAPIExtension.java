package io.smallrye.llm.extensions.impl.azureopenai;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

import org.jboss.logging.Logger;

public class AzureOpenAPIExtension implements BuildCompatibleExtension {
    private static final Logger LOGGER = Logger.getLogger(AzureOpenAPIExtension.class);

    @SuppressWarnings("unused")
    @Discovery
    public void registerCDIComponents(ScannedClasses scannedClasses) {
        LOGGER.info("Add AzureOpenAiChatModelProducer");
        scannedClasses.add(AzureOpenAiChatModelProducer.class.getName());
    }

}
