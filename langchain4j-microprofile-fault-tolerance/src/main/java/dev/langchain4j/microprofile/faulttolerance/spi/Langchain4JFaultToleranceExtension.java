package dev.langchain4j.microprofile.faulttolerance.spi;

import java.util.logging.Logger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessSyntheticBean;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

import dev.langchain4j.core.spi.portableextension.LangChain4JAIServiceBean;
import dev.langchain4j.spi.RegisterAIService;
import io.smallrye.faulttolerance.FaultToleranceBinding;
import io.smallrye.faulttolerance.autoconfig.FaultToleranceMethod;
import io.smallrye.faulttolerance.config.FaultToleranceMethods;
import io.smallrye.faulttolerance.config.FaultToleranceOperation;

/**
 * @author Buhake Sindi
 * @since 29 November 2024
 */
public class Langchain4JFaultToleranceExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(Langchain4JFaultToleranceExtension.class.getName());

    <X> void validateFaultToleranceOperations(@Observes ProcessSyntheticBean<X> event, BeanManager bm) {
        Bean<X> bean = event.getBean();
        LOGGER.info("validateFaultToleranceOperations: Synthetic Event -> " + bean.getBeanClass());
        try {
            AnnotatedType<?> annotatedType = bm.createAnnotatedType(bean.getBeanClass());
            for (AnnotatedMethod<?> annotatedMethod : annotatedType.getMethods()) {
                FaultToleranceMethod method = FaultToleranceMethods.create(annotatedType.getJavaClass(), annotatedMethod);
                if (method.isLegitimate()) {
                    FaultToleranceOperation operation = FaultToleranceOperation.create(method);
                    operation.validate();
                    LOGGER.info("Found: " + operation);

                    if (bean.getStereotypes().contains(RegisterAIService.class)) {
                        //Add Fault Tolerance interceptor Binding
                        ((LangChain4JAIServiceBean<X>) bean).getInterceptorBindings()
                                .add(FaultToleranceBinding.Literal.INSTANCE);
                    }
                }
            }
        } catch (FaultToleranceDefinitionException e) {
            // TODO Auto-generated catch block
            throw new DeploymentException(e);
        }
    }
}
