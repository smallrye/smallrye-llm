package dev.langchain4j.core.spi.portableextension;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.jboss.logging.Logger;

import dev.langchain4j.spi.RegisterAIService;

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
            Class<?> rawType = Reflections.getRawType(event.getInjectionPoint().getType());
            if (classSatisfies(rawType, RegisterAIService.class))
                detectedAIServicesDeclaredInterfaces.add(rawType);
        }

        if (Instance.class.equals(Reflections.getRawType(event.getInjectionPoint().getType()))) {
            Class<?> parameterizedType = Reflections.getRawType(getFacadeType(event.getInjectionPoint()));
            if (classSatisfies(parameterizedType, RegisterAIService.class))
                detectedAIServicesDeclaredInterfaces.add(parameterizedType);
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
            throws ClassNotFoundException {
        for (Class<?> aiServiceClass : detectedAIServicesDeclaredInterfaces) {
            LOGGER.info("afterBeanDiscovery create synthetic:  " + aiServiceClass.getName());
            afterBeanDiscovery.addBean(new LangChain4JAIServiceBean<>(aiServiceClass, beanManager));
        }
    }

    private <T extends Annotation> boolean classSatisfies(Class<?> clazz, Class<T> annotationClass) {
        if (!clazz.isInterface())
            return false;
        T annotation = clazz.getAnnotation(annotationClass);
        return (annotation != null);
    }

    private Type getFacadeType(InjectionPoint injectionPoint) {
        Type genericType = injectionPoint.getType();
        if (genericType instanceof ParameterizedType) {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
        return null;
    }
}
