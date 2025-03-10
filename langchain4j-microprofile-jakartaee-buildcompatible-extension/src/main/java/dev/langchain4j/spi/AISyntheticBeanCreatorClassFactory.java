/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package dev.langchain4j.spi;

import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

public interface AISyntheticBeanCreatorClassFactory extends Comparable<AISyntheticBeanCreatorClassFactory> {

    Class<? extends SyntheticBeanCreator<Object>> getSyntheticBeanCreatorClass();

    /**
     * The priority for the BeanCreatorFactory when resolving the service to get
     * the implementation. This is used when selecting the implementation when
     * several implementations are loaded. The highest priority implementation
     * will be used.
     *
     * @return the priority.
     */
    int getPriority();

    @Override
    default int compareTo(AISyntheticBeanCreatorClassFactory other) {
        return this.getPriority() - other.getPriority();
    }
}
