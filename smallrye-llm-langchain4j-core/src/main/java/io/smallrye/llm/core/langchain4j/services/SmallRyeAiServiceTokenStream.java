package io.smallrye.llm.core.langchain4j.services;

import java.util.function.Consumer;

import org.eclipse.microprofile.ai.llm.TokenStream;

/**
 * @author Buhake Sindi
 * @since 10 October 2024
 */
public class SmallRyeAiServiceTokenStream implements TokenStream {

    private dev.langchain4j.service.TokenStream delegateTokenStream;

    /**
     * @param delegateTokenStream
     */
    public SmallRyeAiServiceTokenStream(dev.langchain4j.service.TokenStream delegateTokenStream) {
        super();
        this.delegateTokenStream = delegateTokenStream;
    }

    //	/* (non-Javadoc)
    //	 * @see io.smallrye.llm.service.TokenStream#onRetrieved(java.util.function.Consumer)
    //	 */
    //	@Override
    //	public TokenStream onRetrieved(Consumer<List<Content>> contentHandler) {
    //		// TODO Auto-generated method stub
    //		delegateTokenStream.onRetrieved(contentHandler);
    //		return this;
    //	}

    /*
     * (non-Javadoc)
     *
     * @see io.smallrye.llm.service.TokenStream#onNext(java.util.function.Consumer)
     */
    @Override
    public TokenStream onNext(Consumer<String> tokenHandler) {
        // TODO Auto-generated method stub
        delegateTokenStream.onNext(tokenHandler);
        return this;
    }

    //	/* (non-Javadoc)
    //	 * @see io.smallrye.llm.service.TokenStream#onComplete(java.util.function.Consumer)
    //	 */
    //	@Override
    //	public TokenStream onComplete(Consumer<Response<AiMessage>> completionHandler) {
    //		// TODO Auto-generated method stub
    //		delegateTokenStream.onComplete(completionHandler);
    //		return this;
    //	}

    /*
     * (non-Javadoc)
     *
     * @see io.smallrye.llm.service.TokenStream#onError(java.util.function.Consumer)
     */
    @Override
    public TokenStream onError(Consumer<Throwable> errorHandler) {
        // TODO Auto-generated method stub
        delegateTokenStream.onError(errorHandler);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.smallrye.llm.service.TokenStream#ignoreErrors()
     */
    @Override
    public TokenStream ignoreErrors() {
        // TODO Auto-generated method stub
        delegateTokenStream.ignoreErrors();
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.smallrye.llm.service.TokenStream#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub
        delegateTokenStream.start();
    }
}
