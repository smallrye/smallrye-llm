package dev.langchain4j.core.config.spi;

import jakarta.enterprise.inject.Instance;

/**
 * Simple function to produce synthetics beans via BeanData
 *
 * @param <R> the result.
 */
@FunctionalInterface
public interface ProducerFunction<R> {
    /**
     * Produces a bean using its name and a lookup context.
     *
     * @param lookup: lookup context.
     * @param beanName: the name of the bean.
     * @return the created bean.
     */
    R produce(Instance<R> lookup, String beanName);
}
