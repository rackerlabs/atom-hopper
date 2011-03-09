package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.util.RegexList;


import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

/**
 *
 * 
 */
public abstract class TargetAwareAbstractCollectionAdapter extends AbstractCollectionAdapter {

    private final RegexList adapterTargets;

    public TargetAwareAbstractCollectionAdapter() {
        adapterTargets = new RegexList();
    }

    public void addTargetRegex(String targetRegex) {
        adapterTargets.add(targetRegex);
    }

    public boolean canHandleTarget(String targetUri) {
        return adapterTargets.matches(targetUri) != null;
    }
}
