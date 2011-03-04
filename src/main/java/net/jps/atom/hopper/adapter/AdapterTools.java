package net.jps.atom.hopper.client.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;

public interface AdapterTools {

    Parser getAtomParser();

    Feed newFeed();

    Entry newEntry();
}
