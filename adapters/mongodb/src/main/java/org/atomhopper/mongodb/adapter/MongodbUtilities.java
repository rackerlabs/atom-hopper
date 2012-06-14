
package org.atomhopper.mongodb.adapter;

public class MongodbUtilities {

    private MongodbUtilities() {
        throw new AssertionError();
    }

    protected static int safeLongToInt(long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (value + " cannot be cast to int without changing its value.");
        }
        return (int) value;
    }

    protected static String formatCollectionName(final String collection) {
        // Note: The maximum size of a collection name is 128 characters
        // (including the name of the db and indexes).
        // It is probably best to keep it under 80/90 chars.
        // http://www.mongodb.org/display/DOCS/Collections
        final int maxCollectionNameLength = 70;

        if(collection.length() > maxCollectionNameLength) {
            return collection.replace('/', '.').substring(0, maxCollectionNameLength);
        } else {
            return collection.replace('/', '.');
        }
    }
}
