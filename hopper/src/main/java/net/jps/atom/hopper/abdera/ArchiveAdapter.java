package net.jps.atom.hopper.abdera;

import net.jps.atom.hopper.abdera.response.ResponseHandler;
import net.jps.atom.hopper.abdera.response.StaticFeedResponseHandler;
import net.jps.atom.hopper.adapter.archive.FeedArchiveSource;
import net.jps.atom.hopper.adapter.request.impl.GetFeedArchiveRequestImpl;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;

/**
 *
 * 
 */
public class ArchiveAdapter extends TargetAwareAbstractCollectionAdapter {

    private final FeedAdapter feedAdapter;
    private final FeedArchiveSource archiveSource;
    private final ResponseHandler<Feed> feedResponseHandler;

    public ArchiveAdapter(FeedArchiveSource archiveSource, FeedAdapter feedAdapter) {
        this.feedAdapter = feedAdapter;
        this.archiveSource = archiveSource;

        feedResponseHandler = new StaticFeedResponseHandler();
    }

    @Override
    public String getAuthor(RequestContext rc) throws ResponseContextException {
        return feedAdapter.getAuthor(rc);
    }

    @Override
    public String getId(RequestContext rc) {
        return feedAdapter.getId(rc);
    }

    @Override
    public String getTitle(RequestContext rc) {
        return feedAdapter.getTitle(rc);
    }

    @Override
    public ResponseContext deleteEntry(RequestContext rc) {
        return ProviderHelper.notsupported(rc);
    }

    @Override
    public ResponseContext getEntry(RequestContext rc) {
        return ProviderHelper.notsupported(rc);
    }

    @Override
    public ResponseContext getFeed(RequestContext rc) {
        try {
            return feedResponseHandler.handleAdapterResponse(rc, archiveSource.getFeed(new GetFeedArchiveRequestImpl(rc)));
        } catch (UnsupportedOperationException uoe) {
            return ProviderHelper.notallowed(rc, uoe.getMessage(), new String[0]); //TODO: Fix this var-args bullshit
        } catch (Exception ex) {
            return ProviderHelper.servererror(rc, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext rc) {
        return ProviderHelper.notsupported(rc);
    }

    @Override
    public ResponseContext putEntry(RequestContext rc) {
        return ProviderHelper.notsupported(rc);
    }
}
