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
public class FeedArchivalFutureImpl implements FeedArchivalFuture {

    private function completionCallback;

    @Override
    public function getCompletionCallback() {
        return completionCallback;
    }

    @Override
    public boolean hasCompletionCallback() {
        return completionCallback != null;
    }

    @Override
    public void onCompletion(function f) {
        completionCallback = f;
    }
}
