/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.client.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public interface AdapterTools {

    Feed newFeed();

    Entry newEntry();
}
