/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.archive;

import net.jps.fava.function;

/**
 *
 * @author zinic
 */
public interface FeedArchivalFuture {

    void onCompletion(function f);

    boolean hasCompletionCallback();
    
    function getCompletionCallback();
}
