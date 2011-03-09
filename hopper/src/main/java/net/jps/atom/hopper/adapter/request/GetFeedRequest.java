package net.jps.atom.hopper.adapter.request;

import java.util.List;

/**
 *
 * 
 */
public interface GetFeedRequest extends ClientRequest {

    List<String> getCategories();

    String getPageMarker();
}
