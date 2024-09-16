package io.smallrye.llm.core.langchain4j.spi.liberty.cdi.internal;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import io.openliberty.cdi.spi.CDIExtensionMetadata;
import io.smallrye.llm.core.langchain4j.portableextension.LangChain4JAIServicePortableExtension;
import io.smallrye.llm.core.langchain4j.portableextension.LangChain4JPluginsPortableExtension;
import io.smallrye.llm.spi.RegisterAIService;
import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Buhake Sindi
 * @since 26 August 2024
 */
@Component(service = CDIExtensionMetadata.class, configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public class Langchain4JCDIExtensionMetadata implements CDIExtensionMetadata {

	/* (non-Javadoc)
	 * @see io.openliberty.cdi.spi.CDIExtensionMetadata#getBeanDefiningAnnotationClasses()
	 */
	@Override
	public Set<Class<? extends Annotation>> getBeanDefiningAnnotationClasses() {
		// TODO Auto-generated method stub
		return Set.of(RegisterAIService.class);
	}

	/* (non-Javadoc)
	 * @see io.openliberty.cdi.spi.CDIExtensionMetadata#getExtensions()
	 */
	@Override
	public Set<Class<? extends Extension>> getExtensions() {
		// TODO Auto-generated method stub
		return Set.of(LangChain4JAIServicePortableExtension.class, LangChain4JPluginsPortableExtension.class);
	}
}
