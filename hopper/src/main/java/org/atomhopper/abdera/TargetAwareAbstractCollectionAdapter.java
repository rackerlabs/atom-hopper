package org.atomhopper.abdera;

import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

public abstract class TargetAwareAbstractCollectionAdapter extends AbstractCollectionAdapter {

    private final String target;

    public TargetAwareAbstractCollectionAdapter(String collectionSpec) {
        this.target = collectionSpec;
    }

    public String getTarget() {
        return target;
    }
}
