package org.eclipse.microprofile.ai.llm;

import java.lang.reflect.Type;

/**
 * @author Buhake Sindi
 * @since 11 October 2024
 */
public interface TokenStreamAdapter {

    boolean canAdaptTokenStreamTo(Type type);

    Object adapt(TokenStream tokenStream);
}
