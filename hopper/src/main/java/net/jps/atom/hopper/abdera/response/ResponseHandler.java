/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.abdera.response;

import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

/**
 *
 * @author zinic
 */
public interface ResponseHandler<T> {

    ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<T> adapterResponse);
}
