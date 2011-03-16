package net.jps.atom.hopper.util.uri.template;

import java.util.Map;

public interface TemplateParameters<T extends Enum<?>> {

    void set(URITemplateParameter parameter, Object value);

    Map<String, Object> toMap();

    T getTargetTemplateKey();
}
