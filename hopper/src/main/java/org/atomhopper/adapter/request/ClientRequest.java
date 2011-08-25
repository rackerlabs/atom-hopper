package org.atomhopper.adapter.request;

import org.atomhopper.util.uri.template.TemplateParameters;
import org.apache.abdera.protocol.server.RequestContext;

/**
 * A client request contains only the bare minimum needed to express the request
 * contents; in this case by wrapping an Abdera RequestContext.
 */
public interface ClientRequest {

    String urlFor(TemplateParameters param);

    RequestContext getRequestContext();
}
