package net.jps.atom.hopper.util.uri;

import java.util.Map;

public interface TemplateParameters<T extends Enum<?>> {

    Map<String, Object> getParameters();

    T getTargetTemplateKey();
}
