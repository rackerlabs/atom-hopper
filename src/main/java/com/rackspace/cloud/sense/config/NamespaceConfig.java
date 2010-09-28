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
import java.util.List;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;

/**
 *
 * @author zinic
 */
public class NamespaceConfig {

    private final RegexTargetResolver regexTargetResolver;
    private final TemplateTargetBuilder templateTargetBuilder;
    private final List<SenseFeedAdapter> namespaceCollectionAdapters;
    private final SimpleWorkspaceInfo workspace;

    public NamespaceConfig(RegexTargetResolver regexTargetResolver, TemplateTargetBuilder templateTargetBuilder, List<SenseFeedAdapter> namespaceCollectionAdapters, SimpleWorkspaceInfo workspace) {
        this.regexTargetResolver = regexTargetResolver;
        this.templateTargetBuilder = templateTargetBuilder;
        this.namespaceCollectionAdapters = namespaceCollectionAdapters;
        this.workspace = workspace;
    }

    public List<SenseFeedAdapter> getRegisteredAdapters() {
        return namespaceCollectionAdapters;
    }

    public RegexTargetResolver getRegexTargetResolver() {
        return regexTargetResolver;
    }

    public TemplateTargetBuilder getTemplateTargetBuilder() {
        return templateTargetBuilder;
    }

    public WorkspaceInfo getWorkspace() {
        return workspace;
    }
}
