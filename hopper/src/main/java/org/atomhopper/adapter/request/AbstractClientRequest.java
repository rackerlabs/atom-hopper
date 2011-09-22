package org.atomhopper.adapter.request;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.util.uri.template.TemplateParameters;

import java.util.List;

/**
 * Base class that contains the wrapped RequestContext from Abdera. All domain
 * specific request classes should inherit from this class.
 */
public abstract class AbstractClientRequest implements ClientRequest {

    private final RequestContext abderaRequestContext;

    protected AbstractClientRequest(RequestContext abderaRequestContext) {
        this.abderaRequestContext = abderaRequestContext;
    }

    protected RequestContext getRequestContext() {
        return abderaRequestContext;
    }
    
    @Override
    public String urlFor(TemplateParameters param) {
        return abderaRequestContext.urlFor(param.getTargetTemplateKey(), param);
    }
    
    @Override
    public Abdera getAbdera() {
        return abderaRequestContext.getAbdera();
    }

    @Override
    public String getTargetParameter(String parameter) {
        return abderaRequestContext.getTarget().getParameter(parameter);
    }
    
    @Override
    public String getRequestParameter(String parameter) {
        return abderaRequestContext.getParameter(parameter);
    }

    @Override
    public List<String> getRequestParameters(String parameter) {
        return abderaRequestContext.getParameters(parameter);
    }
}
