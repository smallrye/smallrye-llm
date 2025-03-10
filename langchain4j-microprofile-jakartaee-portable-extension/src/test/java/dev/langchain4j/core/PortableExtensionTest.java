package dev.langchain4j.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import dev.langchain4j.core.spi.portableextension.LangChain4JAIServicePortableExtension;
import dev.langchain4j.core.spi.portableextension.LangChain4JPluginsPortableExtension;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.smallrye.config.inject.ConfigExtension;

@ExtendWith(WeldJunit5Extension.class)
public class PortableExtensionTest {

    @Inject
    MyDummyAIService myDummyAIService;

    @Inject
    MyDummyApplicationScopedAIService myDummyApplicationScopedAIService;

    @Inject
    RequestContextCaller requestContextCaller;

    @Inject
    ChatLanguageModel chatLanguageModel;

    @Inject
    BeanManager beanManager;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            LangChain4JAIServicePortableExtension.class,
            LangChain4JPluginsPortableExtension.class,
            MyDummyAIService.class,
            MyDummyApplicationScopedAIService.class,
            RequestContextCaller.class,
            DummyChatLanguageModel.class,
            DummyEmbeddingStore.class,
            DummyEmbeddingModel.class,
            ConfigExtension.class)
            .build();

    @Test
    public void assertPlugin() {
        Assertions.assertEquals(((DummyChatLanguageModel) chatLanguageModel).getApiKey(), "apikey");
        Assertions.assertNotNull(((DummyChatLanguageModel) chatLanguageModel).getEmbeddingModel());
        Assertions.assertNotNull(((DummyChatLanguageModel) chatLanguageModel).getEmbeddingModel2());
    }

    @Test
    void detectAIServiceInterface() {
        Assertions.assertTrue(
                LangChain4JAIServicePortableExtension
                        .getDetectedAIServicesDeclaredInterfaces()
                        .contains(MyDummyAIService.class));
        Assertions.assertTrue(
                LangChain4JAIServicePortableExtension
                        .getDetectedAIServicesDeclaredInterfaces()
                        .contains(MyDummyApplicationScopedAIService.class));
    }

    @Test
    void ensureInjectAndScope() {
        Assertions.assertNotNull(myDummyAIService);
        Assertions.assertNotNull(myDummyApplicationScopedAIService);
        assertBeanScope(MyDummyAIService.class, RequestScoped.class);
        assertBeanScope(MyDummyApplicationScopedAIService.class, ApplicationScoped.class);
    }

    @Test
    void callEffectiveCreation() {
        Assertions.assertNotNull(requestContextCaller.run(() -> myDummyAIService.toString()));

    }

    @ActivateRequestContext
    public static class RequestContextCaller {
        public <T> T run(Callable<T> callable) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    private void assertBeanScope(Class<?> beanType, Class<?> scopedClass) {
        Class<? extends Annotation> scope = beanManager.getBeans(beanType).iterator().next().getScope();
        Assertions.assertTrue(scope.isAssignableFrom(scopedClass));
    }

}
