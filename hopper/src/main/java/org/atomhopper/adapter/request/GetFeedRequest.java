package net.jps.atom.hopper.adapter.request;

import java.util.List;
import org.apache.abdera.model.Feed;

/**
 *
 *
 */
public interface GetFeedRequest extends ClientRequest {

    Feed newFeed();

    List<String> getCategories();

    String getPageMarker();
}
