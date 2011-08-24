package net.jps.atom.hopper.adapter.request;

import net.jps.atom.hopper.util.uri.template.TemplateParameters;
import org.apache.abdera.protocol.server.RequestContext;

/**
 * Base class that contains the wrapped RequestContext from Abdera. All domain
 * specific request classes should inherit from this class.
 */
public abstract class AbstractClientRequest implements ClientRequest {

    private final RequestContext abderaRequestContext;

    public AbstractClientRequest(RequestContext abderaRequestContext) {
        this.abderaRequestContext = abderaRequestContext;
    }

    @Override
    public String urlFor(TemplateParameters param) {
        return abderaRequestContext.urlFor(param.getTargetTemplateKey(), param);
    }

    @Override
    public RequestContext getRequestContext() {
        return abderaRequestContext;
    }
}
