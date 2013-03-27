package org.atomhopper.abdera;

import org.apache.abdera.model.Workspace;
import org.apache.abdera.parser.stax.FOMWorkspace;
import org.apache.abdera.protocol.server.CollectionInfo;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.atomhopper.config.v1_0.WorkspaceConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class WorkspaceHandler implements WorkspaceInfo {

    private final Map<String, TargetAwareAbstractCollectionAdapter> collectionAdapterMap;
    private final WorkspaceConfiguration myConfig;

    public WorkspaceHandler(WorkspaceConfiguration myConfig) {
        this.myConfig = myConfig;
        this.collectionAdapterMap = new HashMap<String, TargetAwareAbstractCollectionAdapter>();
    }

    public void addCollectionAdapter(String collectionId, TargetAwareAbstractCollectionAdapter adapter) {
        collectionAdapterMap.put(collectionId, adapter);
    }

    public TargetAwareAbstractCollectionAdapter getAnsweringAdapter(RequestContext rc) {
        final String feedSpec = rc.getTarget().getParameter(TargetResolverField.FEED.toString());
        final String adapterKey = new StringBuilder().append("/")
                 .append(rc.getTarget().getParameter(TargetResolverField.WORKSPACE.toString()))
                 .append("/")
                 .append(feedSpec).toString();
        
        TargetAwareAbstractCollectionAdapter collectionAdapter = null;
        Set<Entry<String, TargetAwareAbstractCollectionAdapter>> adapterEntries = collectionAdapterMap.entrySet();
        
        for(Entry<String, TargetAwareAbstractCollectionAdapter> adapterEntry : adapterEntries){
                if(adapterKey.matches(adapterEntry.getKey())){
                        collectionAdapter = adapterEntry.getValue();
                        break;
                }
        }
        return collectionAdapter;
    }

    @Override
    public Workspace asWorkspaceElement(RequestContext rc) {
        return new FOMWorkspace(myConfig.getTitle());
    }

    @Override
    public Collection<CollectionInfo> getCollections(RequestContext rc) {
        return (Collection) Collections.unmodifiableCollection(collectionAdapterMap.values());
    }

    @Override
    public String getTitle(RequestContext rc) {
        return myConfig.getTitle();
    }
}
