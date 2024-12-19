package io.smallrye.llm.core.langchain4j.portableextension;

import java.util.logging.Logger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessSyntheticBean;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

import io.smallrye.faulttolerance.autoconfig.FaultToleranceMethod;
import io.smallrye.faulttolerance.config.FaultToleranceMethods;
import io.smallrye.faulttolerance.config.FaultToleranceOperation;

/**
 * @author Buhake Sindi
 * @since 29 November 2024
 */
public class Langchain4JFaultToleranceExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(Langchain4JFaultToleranceExtension.class.getName());

    void validateFaultToleranceOperations(@Observes ProcessSyntheticBean<?> event, BeanManager bm) {
        LOGGER.info("validateFaultToleranceOperations: Synthetic Event -> " + event.getBean().getBeanClass());
        try {
            AnnotatedType<?> annotatedType = bm.createAnnotatedType(event.getBean().getBeanClass());
            for (AnnotatedMethod<?> annotatedMethod : annotatedType.getMethods()) {
                FaultToleranceMethod method = FaultToleranceMethods.create(annotatedType.getJavaClass(), annotatedMethod);
                if (method.isLegitimate()) {
                    FaultToleranceOperation operation = FaultToleranceOperation.create(method);
                    operation.validate();
                    LOGGER.info("Found: " + operation);
                }
            }
        } catch (FaultToleranceDefinitionException e) {
            // TODO Auto-generated catch block
            throw new DeploymentException(e);
        }
    }
}
