package net.jps.atom.hopper.util.uri;

import java.util.HashMap;
import java.util.Map;

public class URITemplateParameters<T extends Enum<?>> implements TemplateParameters {

    private final T temlpateTargetKey;
    private final Map<String, Object> parameterMap;

    public URITemplateParameters(T temlpateTargetKey) {
        this(temlpateTargetKey, new HashMap<String, Object>());
    }

    public URITemplateParameters(T temlpateTargetKey, Map<String, Object> parameterMap) {
        this.temlpateTargetKey = temlpateTargetKey;
        this.parameterMap = parameterMap;
    }

    public void setEntryResource(String entryResource) {
        parameterMap.put("entry", entryResource);
    }

    public void setMarker(String marker) {
        parameterMap.put("lochint", marker);
    }

    public void setLimit(String limit) {
        parameterMap.put("limit", limit);
    }

    @Override
    public Map<String, Object> getParameters() {
        return new HashMap(parameterMap);
    }

    @Override
    public T getTargetTemplateKey() {
        return temlpateTargetKey;
    }
}
