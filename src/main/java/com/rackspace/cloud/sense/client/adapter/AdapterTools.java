package com.rackspace.cloud.sense.client.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

public interface AdapterTools {

    Feed newFeed();

    Entry newEntry();
}
