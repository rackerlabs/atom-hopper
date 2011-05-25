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

    private static final String NEXT_LINK = "next";
    private static final String PREV_LINK = "prev";
    private static final String CURRENT_LINK = "current";


    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();
        final String self = StringUtils.join(new String[]{rc.getBaseUri().toString(), rc.getTargetPath()});
        final int numEntries = f.getEntries().size();

        if (numEntries == 0) {
            return;
        }

        if (linkNotSet(f, CURRENT_LINK)) {
            f.addLink(self, CURRENT_LINK);
        }

        if (linkNotSet(f, NEXT_LINK) && linkNotSet(f, PREV_LINK)) {
            final Entry last = f.getEntries().get(numEntries - 1);

            if (last.getId() != null) {
                final String nextMarker = last.getId().toString();
                f.addLink(StringUtils.join(new String[]{self, "?marker=", nextMarker}), NEXT_LINK);
            }

            final Entry first = f.getEntries().get(0);
            if (first.getId() != null) {
                final String previousMarker = first.getId().toString();
                f.addLink(StringUtils.join(new String[]{self, "?marker=", previousMarker}), PREV_LINK);
            }
        }


    }

    private boolean linkNotSet(Feed feed, String link) {
        return (feed.getLinks(link).size() == 0);
    }
}
