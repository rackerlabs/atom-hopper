package org.atomhopper.adapter.request;

import java.util.Calendar;

/**
 *
 * 
 */
public interface GetFeedArchiveRequest extends ClientRequest {

    Calendar getRequestedArchiveDate();
}
