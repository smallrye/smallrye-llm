package io.smallrye.llm.core.langchain4j.portableextension;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.jboss.logging.Logger;

import io.smallrye.llm.aiservice.CommonAIServiceCreator;
import io.smallrye.llm.spi.RegisterAIService;

public class LangChain4JAIServicePortableExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(LangChain4JAIServicePortableExtension.class);
    private static final Set<Class<?>> detectedAIServicesDeclaredInterfaces = new HashSet<>();

    public static Set<Class<?>> getDetectedAIServicesDeclaredInterfaces() {
        return detectedAIServicesDeclaredInterfaces;
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations({ RegisterAIService.class }) ProcessAnnotatedType<T> pat) {
        if (pat.getAnnotatedType().getJavaClass().isInterface()) {
            LOGGER.info("processAnnotatedType register " + pat.getAnnotatedType().getJavaClass().getName());
            detectedAIServicesDeclaredInterfaces.add(pat.getAnnotatedType().getJavaClass());
        } else {
            LOGGER.warn("processAnnotatedType reject " + pat.getAnnotatedType().getJavaClass().getName()
                    + " which is not an interface");
            pat.veto();
        }
    }

    /**
     * This is useful for application servers that can't support proccessAnnotatedType.
     *
     * @param event
     */
    void processInjectionPoints(@Observes ProcessInjectionPoint<?, ?> event) {
        if (event.getInjectionPoint().getBean() == null) {
            processInjectionPoint(event);
        }

        if (Instance.class.equals(Reflections.getRawType(event.getInjectionPoint().getType()))) {
            processInjectionPoint(event);
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
            throws ClassNotFoundException {
        for (Class<?> aiServiceClass : detectedAIServicesDeclaredInterfaces) {
            LOGGER.info("afterBeanDiscovery create synthetic :  " + aiServiceClass.getName());
            final RegisterAIService annotation = aiServiceClass.getAnnotation(RegisterAIService.class);
            afterBeanDiscovery.addBean()
                    .types(aiServiceClass)
                    .scope(annotation.scope())
                    .name("registeredAIService-" + aiServiceClass.getName()) //Without this, the container won't create a CreationalContext
                    .createWith(creationalContext -> CommonAIServiceCreator.create(CDI.current(), aiServiceClass));
        }
    }

    private void processInjectionPoint(ProcessInjectionPoint<?, ?> event) {
        Class<?> rawType = Reflections.getRawType(event.getInjectionPoint().getType());
        if (rawType.isInterface()) {
            RegisterAIService annotation = rawType.getAnnotation(RegisterAIService.class);
            if (annotation != null) {
                detectedAIServicesDeclaredInterfaces.add(rawType);
            } else {
            	LOGGER.warn("Detected interface '" + rawType.getName() + "' has @" + RegisterAIService.class.getSimpleName() + " annotation. Ignoring...");
            }
        }
    }
}
