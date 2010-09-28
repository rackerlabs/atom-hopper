/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package com.rackspace.cloud.sense.abdera;

import com.rackspace.cloud.sense.config.NamespaceConfigProcessor;
import com.rackspace.cloud.sense.config.NamespaceConfig;
import com.rackspace.cloud.sense.config.SenseNamespaceConfiguration;
import com.rackspace.cloud.sense.context.ApplicationContextAdapter;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.impl.AbstractWorkspaceProvider;

public class SenseProvider extends AbstractWorkspaceProvider {

    private final NamespaceConfig namespaceConfig;

    public SenseProvider(SenseNamespaceConfiguration config, ApplicationContextAdapter contextAdapter, Abdera abdera) {
        this.abdera = abdera;

        namespaceConfig = new NamespaceConfigProcessor(config, contextAdapter, abdera).toConfig();

        setTargetResolver(namespaceConfig.getRegexTargetResolver());
        setTargetBuilder(namespaceConfig.getTemplateTargetBuilder());

        addWorkspace(namespaceConfig.getWorkspace());
    }

    @Override
    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        for (SenseFeedAdapter currentAdapter : namespaceConfig.getRegisteredAdapters()) {
            if (currentAdapter.handles(request.getTargetPath())) {
                return currentAdapter;
            }
        }

        return null;
    }
    
//    public class SimpleFilter implements Filter {
//        public ResponseContext filter(RequestContext request, FilterChain chain) {
//            RequestContextWrapper rcw = new RequestContextWrapper(request);
//            rcw.setAttribute("offset", 10);
//            rcw.setAttribute("count", 10);
//            return chain.next(rcw);
//        }
//    }
}
