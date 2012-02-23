package org.atomhopper.adapter.request.adapter;

import org.atomhopper.adapter.request.feed.FeedRequest;

import java.util.Calendar;

/**
 *
 * 
 */
public interface GetFeedArchiveRequest extends FeedRequest {

    Calendar getRequestedArchiveDate();
}
