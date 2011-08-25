package org.atomhopper.adapter.request.adapter;

import java.util.Calendar;
import org.atomhopper.adapter.request.feed.FeedRequest;

/**
 *
 * 
 */
public interface GetFeedArchiveRequest extends FeedRequest {

    Calendar getRequestedArchiveDate();
}
