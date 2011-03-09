/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request;

import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public abstract class AbstractClientRequest implements ClientRequest {

    private final RequestContext abderaRequestContext;

    public AbstractClientRequest(RequestContext abderaRequestContext) {
        this.abderaRequestContext = abderaRequestContext;
    }

    @Override
    public RequestContext getRequestContext() {
        return abderaRequestContext;
    }
}
