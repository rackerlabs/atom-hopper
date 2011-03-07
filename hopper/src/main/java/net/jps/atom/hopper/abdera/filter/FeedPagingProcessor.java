/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.abdera.filter;

import com.rackspace.cloud.commons.util.StringUtilities;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class FeedPagingProcessor implements AdapterResponseProcessor<Feed> {

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();
        final int numEntries = f.getEntries().size();

        if (numEntries > 0) {
            final String nextMarker = f.getEntries().get(numEntries).getId().toString(), previousMarker = f.getEntries().get(0).getId().toString();

            final String self = StringUtilities.join(rc.getBaseUri().toString(), rc.getTargetPath());

            // Add markers
            f.addLink(StringUtilities.join(self), "current");
            f.addLink(StringUtilities.join(self, "?marker=", nextMarker), "next");
            f.addLink(StringUtilities.join(self, "?marker=", previousMarker), "prev");
        }
    }
}
