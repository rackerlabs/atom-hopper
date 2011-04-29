package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.lang.StringUtils;

/**
 *
 *
 */
public class FeedPagingProcessor implements AdapterResponseProcessor<Feed> {

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();
        final int numEntries = f.getEntries().size();

        if (numEntries > 0) {
            final Entry first = f.getEntries().get(0), last = f.getEntries().get(numEntries - 1);

            if (first.getId() != null && last.getId() != null) {
                final String nextMarker = last.getId().toString(), previousMarker = first.getId().toString();

                final String self = StringUtils.join(new String[]{rc.getBaseUri().toString(), rc.getTargetPath()});

                // Add markers
                f.addLink(self, "current");
                f.addLink(StringUtils.join(new String[]{self, "?marker=", nextMarker}), "next");
                f.addLink(StringUtils.join(new String[]{self, "?marker=", previousMarker}), "prev");
            }
        }
    }
}
