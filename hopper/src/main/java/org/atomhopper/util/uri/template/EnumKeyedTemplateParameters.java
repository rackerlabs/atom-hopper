package org.atomhopper.util.uri.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//TODO: Consider making domain scoped child classes with helper methods
//      Something like setMarker(String markerId) or setPageLimit(int pageLimit)
public class EnumKeyedTemplateParameters<T extends Enum> implements TemplateParameters<T> {

    private final T temlpateTargetKey;
    private final Map<String, Object> parameterMap;

    public EnumKeyedTemplateParameters(T temlpateTargetKey) {
        this.temlpateTargetKey = temlpateTargetKey;
        this.parameterMap = new HashMap<String, Object>();
    }

    private EnumKeyedTemplateParameters(T temlpateTargetKey, Map<String, Object> parameterMap) {
        this(temlpateTargetKey);

        this.parameterMap.putAll(parameterMap);
    }

    public EnumKeyedTemplateParameters(T temlpateTargetKey, TemplateParameters<T> parameters) {
        this(temlpateTargetKey, parameters.toMap());
    }

    @Override
    public void set(URITemplateParameter parameter, Object value) {
        parameterMap.put(parameter.toString(), value);
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(parameterMap);
    }

    @Override
    public T getTargetTemplateKey() {
        return temlpateTargetKey;
    }
}
