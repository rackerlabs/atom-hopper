/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public interface AdapterResponseProcessor<T> {

    void process(RequestContext rc, AdapterResponse<T> adapterResponse);
}
