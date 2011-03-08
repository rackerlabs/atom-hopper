/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request;

import java.util.List;

/**
 *
 * @author zinic
 */
public interface GetFeedRequest extends ClientRequest {

    List<String> getCategories();

    String getPageMarker();
}
