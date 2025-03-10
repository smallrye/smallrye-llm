package dev.langchain4j.core.spi.portableextension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.util.AnnotationLiteral;

import dev.langchain4j.aiservice.CommonAIServiceCreator;
import dev.langchain4j.spi.RegisterAIService;

/**
 * @author Buhake Sindi
 * @since 21 November 2024
 */
public class LangChain4JAIServiceBean<T> implements Bean<T>, PassivationCapable {

    private final Class<T> aiServiceInterfaceClass;

    private final BeanManager beanManager;

    private final Class<? extends Annotation> scope;

    private Set<Annotation> interceptorBindings;

    /**
     * @param aiServiceInterfaceClass
     * @param beanManager
     */
    public LangChain4JAIServiceBean(Class<T> aiServiceInterfaceClass, BeanManager beanManager) {
        super();
        final RegisterAIService annotation = (this.aiServiceInterfaceClass = aiServiceInterfaceClass)
                .getAnnotation(RegisterAIService.class);
        this.scope = annotation.scope();
        this.beanManager = beanManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.PassivationCapable#getId()
     */
    @Override
    public String getId() {
        return aiServiceInterfaceClass.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.context.spi.Contextual#create(jakarta.enterprise.context.spi.CreationalContext)
     */
    @Override
    public T create(CreationalContext<T> creationalContext) {
        T instance = CommonAIServiceCreator.create(CDI.current(), aiServiceInterfaceClass);
        if (!getInterceptorBindings().isEmpty()) {
            InterceptionFactory<T> factory = beanManager.createInterceptionFactory(creationalContext, aiServiceInterfaceClass);
            interceptorBindings.stream().forEach(factory.configure()::add);
            instance = factory.createInterceptedInstance(instance);
        }

        return instance;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.context.spi.Contextual#destroy(java.lang.Object,
     * jakarta.enterprise.context.spi.CreationalContext)
     */
    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.BeanAttributes#getTypes()
     */
    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(aiServiceInterfaceClass);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.BeanAttributes#getQualifiers()
     */
    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(new AnnotationLiteral<Default>() {
        });
        annotations.add(new AnnotationLiteral<Any>() {
        });
        return Collections.unmodifiableSet(annotations);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.BeanAttributes#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.BeanAttributes#getName()
     */
    @Override
    public String getName() {
        return "registeredAIService-" + aiServiceInterfaceClass.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.BeanAttributes#getStereotypes()
     */
    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.singleton(RegisterAIService.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.BeanAttributes#isAlternative()
     */
    @Override
    public boolean isAlternative() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.Bean#getBeanClass()
     */
    @Override
    public Class<?> getBeanClass() {
        return aiServiceInterfaceClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.enterprise.inject.spi.Bean#getInjectionPoints()
     */
    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    /**
     * @return the interceptorBindings
     */
    public Set<Annotation> getInterceptorBindings() {
        if (interceptorBindings == null)
            interceptorBindings = new HashSet<>();
        return interceptorBindings;
    }

    @Override
    public String toString() {
        return "AiService [ interfaceType: " + aiServiceInterfaceClass.getSimpleName() + " ] with Qualifiers ["
                + getQualifiers() + "]";
    }
}
