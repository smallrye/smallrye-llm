package dev.langchain4j.cdi.aiservice;

import dev.langchain4j.cdi.aiservice.CommonAIServiceCreator;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

public class AIServiceCreator implements SyntheticBeanCreator<Object> {

    @Override
    public Object create(Instance<Object> lookup, Parameters params) {
        return CommonAIServiceCreator.create(lookup,
                params.get(Langchain4JAIServiceBuildCompatibleExtension.PARAM_INTERFACE_CLASS, Class.class));
    }

}
