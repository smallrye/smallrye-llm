/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package io.smallrye.llm.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

public class AISyntheticBeanCreatorClassProvider {

    private static final AISyntheticBeanCreatorClassFactory factory;

    static {
        ServiceLoader<AISyntheticBeanCreatorClassFactory> loader = ServiceLoader.load(AISyntheticBeanCreatorClassFactory.class,
                Thread.currentThread().getContextClassLoader());
        final List<AISyntheticBeanCreatorClassFactory> factories = new ArrayList<>();
        loader.forEach(factories::add);
        if (factories.isEmpty()) {
            factory = new AISyntheticBeanCreatorClassFactory() {
                @Override
                public Class<? extends SyntheticBeanCreator<Object>> getSyntheticBeanCreatorClass() {
                    try {
                        return (Class<? extends SyntheticBeanCreator<Object>>) Thread.currentThread().getContextClassLoader()
                                .loadClass("io.smallrye.llm.plugin.LLMPluginCreator");
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public int getPriority() {
                    return -1;
                }
            };
        } else {
            Collections.sort(factories);
            factory = factories.get(factories.size() - 1);
        }
    }

    public static Class<? extends SyntheticBeanCreator<Object>> getSyntheticBeanCreatorClass() {
        return factory.getSyntheticBeanCreatorClass();
    }
}
