package com.rackspace.cloud.sense.abdera;

import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;

public class AbderaAdapterTools implements AdapterTools {

    private final Abdera abderaReference;

    public AbderaAdapterTools(Abdera abderaReference) {
        this.abderaReference = abderaReference;
    }

    @Override
    public Parser getAtomParser() {
        return abderaReference.getParser();
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
