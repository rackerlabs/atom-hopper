/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jps.atom.hopper.adapter.request.impl;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetFeedArchiveRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class GetFeedArchiveRequestImpl extends AbstractClientRequest implements GetFeedArchiveRequest {

    public GetFeedArchiveRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Calendar getRequestedArchiveDate() {
        return Calendar.getInstance();
    }
}
