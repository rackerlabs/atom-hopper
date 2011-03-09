package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.util.RegexList;


import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

/**
 *
 * 
 */
public abstract class TargetAwareAbstractCollectionAdapter extends AbstractCollectionAdapter {

    private final RegexList adpterTargets;

    public TargetAwareAbstractCollectionAdapter() {
        adpterTargets = new RegexList();
    }

    public void addTargetRegex(String targetRegex) {
        adpterTargets.matches(targetRegex);
    }

    public boolean canHandleTarget(String targetUri) {
        return adpterTargets.matches(targetUri) != null;
    }
}
