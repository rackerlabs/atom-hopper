/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request;

import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public interface ClientRequest {

    RequestContext getRequestContext();
}
