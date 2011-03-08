/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request.impl;

import java.util.LinkedList;
import java.util.List;
import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetFeedRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class GetFeedRequestImpl extends AbstractClientRequest implements GetFeedRequest {

    public GetFeedRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public List<String> getCategories() {
        return new LinkedList<String>();
    }

    @Override
    public String getPageMarker() {
        return "";
    }
}
