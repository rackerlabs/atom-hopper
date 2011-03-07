package net.jps.atom.hopper.adapter;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;

public class FeedAdapterTools implements AdapterTools {

    private final Abdera abderaReference;

    public FeedAdapterTools(Abdera abderaReference) {
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
