/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.util.RegexList;


import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

/**
 *
 * @author zinic
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
