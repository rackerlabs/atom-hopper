package net.jps.atom.hopper.adapter.request;

import org.apache.abdera.protocol.server.RequestContext;

/**
 * A client request contains only the bare minimum needed to express the request
 * contents; in this case by wrapping an Abdera RequestContext.
 */
public interface ClientRequest {

    RequestContext getRequestContext();
}
