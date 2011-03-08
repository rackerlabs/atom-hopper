/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.abdera.response;

import java.util.Date;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

/**
 *
 * @author zinic
 */
public class StaticEntryResponseHandler extends AbstractResponseHandler<Entry> {

    public StaticEntryResponseHandler() {
        super();
    }

    @Override
    public ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<Entry> adapterResponse) {
        final Date lastUpdated = adapterResponse.getBody() != null ? adapterResponse.getBody().getUpdated() : null;

        switch (adapterResponse.getResponseStatus()) {
            case OK:
            case CREATED:
                return ProviderHelper.returnBase(adapterResponse.getBody(), adapterResponse.getResponseStatus().intValue(), lastUpdated);

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, adapterResponse.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new InternalServerException());

            default:
                return ProviderHelper.notfound(rc);
        }
    }
}
