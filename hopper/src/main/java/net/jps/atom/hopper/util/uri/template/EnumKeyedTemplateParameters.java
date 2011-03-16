package net.jps.atom.hopper.util.uri.template;

import java.util.HashMap;
import java.util.Map;

//TODO: Consider making domain scoped child classes with helper methods
//      Something like setMarker(String markerId) or setPageLimit(int pageLimit)
public class EnumKeyedTemplateParameters<T extends Enum<?>> implements TemplateParameters {

    private final T temlpateTargetKey;
    private final Map<String, Object> parameterMap;

    public EnumKeyedTemplateParameters(T temlpateTargetKey) {
        this(temlpateTargetKey, new HashMap<String, Object>());
    }

    public EnumKeyedTemplateParameters(T temlpateTargetKey, Map<String, Object> parameterMap) {
        this.temlpateTargetKey = temlpateTargetKey;
        this.parameterMap = parameterMap;
    }

    @Override
    public void set(URITemplateParameter parameter, Object value) {
        parameterMap.put(parameter.toString(), value);
    }

    @Override
    public Map<String, Object> toMap() {
        return new HashMap(parameterMap);
    }

    @Override
    public T getTargetTemplateKey() {
        return temlpateTargetKey;
    }
}
