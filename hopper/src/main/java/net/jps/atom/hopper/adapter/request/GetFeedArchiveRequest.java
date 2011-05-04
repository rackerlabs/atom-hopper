package net.jps.atom.hopper.adapter.request;

import org.apache.abdera.model.Feed;

/**
 *
 * 
 */
public interface GetFeedArchiveRequest extends ClientRequest {

    Feed newFeed();

    String getArchiveId();
}
