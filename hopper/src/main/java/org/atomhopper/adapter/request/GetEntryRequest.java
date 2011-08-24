package org.atomhopper.adapter.request;

import org.apache.abdera.model.Entry;

/**
 *
 *
 */
public interface GetEntryRequest extends ClientRequest {
    String getId();

    Entry newEntry();
}
