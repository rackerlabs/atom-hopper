/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request;

import java.util.Calendar;

/**
 *
 * 
 */
public interface GetFeedArchiveRequest extends ClientRequest {

    Calendar getRequestedArchiveDate();
}
