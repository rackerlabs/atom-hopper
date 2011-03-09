/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.PostEntryRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class PostEntryRequestImpl extends AbstractClientRequest implements PostEntryRequest {

    public PostEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }
}
