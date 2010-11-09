/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.abdera;

import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class AbderaAdapterTools implements AdapterTools {

    private final Abdera abderaReference;

    public AbderaAdapterTools(Abdera abderaReference) {
        this.abderaReference = abderaReference;
    }

    @Override
    public Entry newEntry() {
        return abderaReference.newEntry();
    }

    @Override
    public Feed newFeed() {
        return abderaReference.newFeed();
    }
}
