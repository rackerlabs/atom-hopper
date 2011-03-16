package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.RequestProcessor;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.Transactional;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.SimpleSubjectResolver;
import org.apache.abdera.protocol.server.processors.CategoriesRequestProcessor;
import org.apache.abdera.protocol.server.processors.CollectionRequestProcessor;
import org.apache.abdera.protocol.server.processors.EntryRequestProcessor;
import org.apache.abdera.protocol.server.processors.MediaRequestProcessor;
import org.apache.abdera.protocol.server.processors.ServiceRequestProcessor;

public class WorkspaceProvider implements Provider {

    private static final Logger LOG = new RCLogger(WorkspaceProvider.class);
    private final Map<TargetType, RequestProcessor> requestProcessors;
    private final List<Filter> filters;

    private WorkspaceManager workspaceManager;
    private Map<String, String> properties;
    private Abdera abdera;

    public WorkspaceProvider() {
        requestProcessors = new HashMap<TargetType, RequestProcessor>();
        filters = new ArrayList<Filter>();

        // Setting default request processors:
        this.requestProcessors.put(TargetType.TYPE_SERVICE, new ServiceRequestProcessor());
        this.requestProcessors.put(TargetType.TYPE_CATEGORIES, new CategoriesRequestProcessor());
        this.requestProcessors.put(TargetType.TYPE_COLLECTION, new CollectionRequestProcessor());
        this.requestProcessors.put(TargetType.TYPE_ENTRY, new EntryRequestProcessor());
        this.requestProcessors.put(TargetType.TYPE_MEDIA, new MediaRequestProcessor());
    }

    public void setWorkspaceManager(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    public WorkspaceManager getWorkspaceManager() {
        return workspaceManager;
    }

    @Override
    public void init(Abdera abdera, Map<String, String> properties) {
        this.abdera = abdera;
        this.properties = properties;
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    @Override
    public Abdera getAbdera() {
        return abdera;
    }

    @Override
    public Subject resolveSubject(RequestContext request) {
        return new SimpleSubjectResolver().resolve(request);
    }

    @Override
    public Target resolveTarget(RequestContext request) {
        return workspaceManager.resolveTarget(request);
    }

    @Override
    public String urlFor(RequestContext request, Object key, Object param) {
        return workspaceManager.urlFor(request, key, param);
    }

    @Override
    public ResponseContext process(RequestContext request) {
        final Target target = request.getTarget();

        if (target == null || target.getType() == TargetType.TYPE_NOT_FOUND) {
            return ProviderHelper.notfound(request);
        }

        final RequestProcessor processor = this.requestProcessors.get(target.getType());

        if (processor == null) {
            return ProviderHelper.notfound(request);
        }

        final WorkspaceManager wm = getWorkspaceManager();
        final CollectionAdapter adapter = wm.getCollectionAdapter(request);
        final Transactional transaction = adapter instanceof Transactional ? (Transactional) adapter : null;

        ResponseContext response = null;

        try {
            transactionStart(transaction, request);
            response = processor.process(request, wm, adapter);
            response = response != null ? response : processExtensionRequest(request, adapter);
        } catch (Exception ex) {
            return handleAdapterException(ex, transaction, request, response);
        } finally {
            transactionEnd(transaction, request, response);
        }

        return response != null ? response : ProviderHelper.badrequest(request);
    }

    private ResponseContext handleAdapterException(Exception ex, Transactional transaction, RequestContext request, ResponseContext response) {
        if (ex instanceof ResponseContextException) {
            final ResponseContextException rce = (ResponseContextException) ex;

            if (rce.getStatusCode() >= 400 && rce.getStatusCode() < 500) {
                // don't report routine 4xx HTTP errors
                LOG.info(ex);
            } else {
                LOG.error(ex);
            }
        } else {
            LOG.error(ex);
        }

        transactionCompensate(transaction, request, ex);
        return createErrorResponse(request, ex);
    }

    private void transactionCompensate(Transactional transactional, RequestContext request, Throwable e) {
        if (transactional != null) {
            transactional.compensate(request, e);
        }
    }

    private void transactionEnd(Transactional transactional, RequestContext request, ResponseContext response) {
        if (transactional != null) {
            transactional.end(request, response);
        }
    }

    private void transactionStart(Transactional transactional, RequestContext request) throws ResponseContextException {
        if (transactional != null) {
            transactional.start(request);
        }
    }

    protected ResponseContext createErrorResponse(RequestContext request, Throwable e) {
        return ProviderHelper.servererror(request, e);
    }

    private ResponseContext processExtensionRequest(RequestContext context, CollectionAdapter adapter) {
        return adapter.extensionRequest(context);
    }

//    private Service getServiceElement(RequestContext request) {
//        final Service service = abdera.newService();
//
//        for (WorkspaceInfo wi : getWorkspaceManager().getWorkspaces(request)) {
//            service.addWorkspace(wi.asWorkspaceElement(request));
//        }
//
//        return service;
//    }

    public void setFilters(List<Filter> filters) {
        this.filters.clear();
        this.filters.addAll(filters);
    }

    @Override
    public Filter[] getFilters(RequestContext request) {
        return filters.toArray(new Filter[filters.size()]);
    }

    public void addFilter(Filter... filters) {
        this.filters.addAll(Arrays.asList(filters));
    }

    @Override
    public void setRequestProcessors(Map<TargetType, RequestProcessor> requestProcessors) {
        this.requestProcessors.clear();
        this.requestProcessors.putAll(requestProcessors);
    }

    @Override
    public void addRequestProcessors(Map<TargetType, RequestProcessor> requestProcessors) {
        this.requestProcessors.putAll(requestProcessors);
    }

    @Override
    public Map<TargetType, RequestProcessor> getRequestProcessors() {
        return Collections.unmodifiableMap(this.requestProcessors);
    }
}
