package net.jps.atom.hopper.adapter.request;

import org.apache.abdera.model.Entry;

/**
 *
 * 
 */
public interface PostEntryRequest extends ClientRequest {
    Entry getEntry();
}
