/*
 *  Copyright 2010 zinic.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.config;

import com.rackspace.cloud.sense.abdera.SenseFeedAdapter;
import com.rackspace.cloud.sense.config.v1_0.WorkspaceConfig;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.protocol.server.CollectionInfo;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetBuilder;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;

/**
 *
 * @author zinic
 */
public class WorkspaceHandler implements WorkspaceInfo {

    private final List<SenseFeedAdapter> namespaceCollectionAdapters;
    private final RegexTargetResolver regexTargetResolver;
    private final TemplateTargetBuilder templateTargetBuilder;
    private final WorkspaceConfig myConfig;

    public WorkspaceHandler(WorkspaceConfig myConfig, RegexTargetResolver regexTargetResolver, TemplateTargetBuilder templateTargetBuilder) {
        this.myConfig = myConfig;
        this.regexTargetResolver = regexTargetResolver;
        this.templateTargetBuilder = templateTargetBuilder;

        this.namespaceCollectionAdapters = new LinkedList<SenseFeedAdapter>();
    }

    public RegexTargetResolver getRegexTargetResolver() {
        return regexTargetResolver;
    }

    public TemplateTargetBuilder getTemplateTargetBuilder() {
        return templateTargetBuilder;
    }

    public void addCollectionAdapter(SenseFeedAdapter adapter) {
        namespaceCollectionAdapters.add(adapter);
    }

    public SenseFeedAdapter getAnsweringAdapter(RequestContext rc) {
        for (SenseFeedAdapter adapter : namespaceCollectionAdapters) {
            if (adapter.handles(rc.getTargetPath())) {
                return adapter;
            }
        }

        return null;
    }

    @Override
    public Workspace asWorkspaceElement(RequestContext rc) {
        return null;
    }

    @Override
    public Collection<CollectionInfo> getCollections(RequestContext rc) {
        return (Collection) namespaceCollectionAdapters;
    }

    @Override
    public String getTitle(RequestContext rc) {
        return myConfig.getTitle();
    }
}
