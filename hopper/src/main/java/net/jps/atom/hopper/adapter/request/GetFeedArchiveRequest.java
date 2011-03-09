package net.jps.atom.hopper.adapter.request;

import java.util.Calendar;

/**
 *
 * 
 */
public interface GetFeedArchiveRequest extends ClientRequest {

    Calendar getRequestedArchiveDate();
}
