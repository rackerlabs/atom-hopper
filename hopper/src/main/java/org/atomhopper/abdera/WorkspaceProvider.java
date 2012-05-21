package org.atomhopper.abdera;

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
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.abdera.protocol.server.impl.SimpleSubjectResolver;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import org.apache.abdera.protocol.server.processors.CategoriesRequestProcessor;
import org.apache.abdera.protocol.server.processors.CollectionRequestProcessor;
import org.apache.abdera.protocol.server.processors.EntryRequestProcessor;
import org.apache.abdera.protocol.server.processors.ServiceRequestProcessor;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;
import org.atomhopper.config.v1_0.HostConfiguration;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.TemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.atomhopper.util.uri.template.URITemplateParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkspaceProvider implements Provider {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceProvider.class);
    private static final int MIN_ERROR_CODE = 400;
    private static final int MAX_ERROR_CODE = 500;
    private final Map<TargetType, RequestProcessor> requestProcessors;
    private final List<Filter> filters;
    private final WorkspaceManager workspaceManager;
    private final RegexTargetResolver targetResolver;
    private final HostConfiguration hostConfiguration;
    private final TemplateTargetBuilder templateTargetBuilder;
    private Map<String, String> properties;
    private Abdera abdera;

    public WorkspaceProvider(HostConfiguration hostConfiguration) {
        requestProcessors = new HashMap<TargetType, RequestProcessor>();
        filters = new LinkedList<Filter>();
        targetResolver = new RegexTargetResolver();

        // Set the host configuration
        this.hostConfiguration = hostConfiguration;

        // Setting default request processors:
        requestProcessors.put(TargetType.TYPE_SERVICE, new ServiceRequestProcessor());
        requestProcessors.put(TargetType.TYPE_CATEGORIES, new CategoriesRequestProcessor());
        requestProcessors.put(TargetType.TYPE_COLLECTION, new CollectionRequestProcessor());
        requestProcessors.put(TargetType.TYPE_ENTRY, new EntryRequestProcessor());

        templateTargetBuilder = new TemplateTargetBuilder();
        templateTargetBuilder.setTemplate(URITemplate.WORKSPACE, URITemplate.WORKSPACE.toString());
        templateTargetBuilder.setTemplate(URITemplate.FEED, URITemplate.FEED.toString());

        workspaceManager = new WorkspaceManager();
    }

    public RegexTargetResolver getTargetResolver() {
        return targetResolver;
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
        return targetResolver.resolve(request);
    }

    @Override
    public String urlFor(RequestContext request, Object key, Object param) {
        final Target resolvedTarget = request.getTarget();

        if (param == null || param instanceof TemplateParameters) {
            final TemplateParameters templateParameters = param != null
                    ? (TemplateParameters) param
                    : new EnumKeyedTemplateParameters((Enum) key);

            templateParameters.set(URITemplateParameter.HOST_DOMAIN, hostConfiguration.getDomain());
            templateParameters.set(URITemplateParameter.HOST_SCHEME, hostConfiguration.getScheme());

            //This is what happens when you don't use enumerations :p
            if (resolvedTarget.getType() == TargetType.TYPE_SERVICE) {
                templateParameters.set(URITemplateParameter.WORKSPACE_RESOURCE, resolvedTarget.getParameter(TargetResolverField.WORKSPACE.toString()));
            } else if (resolvedTarget.getType() == TargetType.TYPE_COLLECTION) {
                templateParameters.set(URITemplateParameter.WORKSPACE_RESOURCE, resolvedTarget.getParameter(TargetResolverField.WORKSPACE.toString()));
                templateParameters.set(URITemplateParameter.FEED_RESOURCE, resolvedTarget.getParameter(TargetResolverField.FEED.toString()));
            } else if (resolvedTarget.getType() == TargetType.TYPE_CATEGORIES) {
                templateParameters.set(URITemplateParameter.WORKSPACE_RESOURCE, resolvedTarget.getParameter(TargetResolverField.WORKSPACE.toString()));
                templateParameters.set(URITemplateParameter.FEED_RESOURCE, resolvedTarget.getParameter(TargetResolverField.FEED.toString()));
            } else if (resolvedTarget.getType() == TargetType.TYPE_ENTRY) {
                templateParameters.set(URITemplateParameter.WORKSPACE_RESOURCE, resolvedTarget.getParameter(TargetResolverField.WORKSPACE.toString()));
                templateParameters.set(URITemplateParameter.FEED_RESOURCE, resolvedTarget.getParameter(TargetResolverField.FEED.toString()));
                templateParameters.set(URITemplateParameter.ENTRY_RESOURCE, resolvedTarget.getParameter(TargetResolverField.ENTRY.toString()));
            }

            return templateTargetBuilder.urlFor(request, key, templateParameters.toMap());
        }

        //Support maps eventually for this
        throw new IllegalArgumentException("URL Generation expects a TemplateParameters object");
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

        final CollectionAdapter adapter = getWorkspaceManager().getCollectionAdapter(request);

        ResponseContext response = null;

        if (adapter != null) {
            final Transactional transaction = adapter instanceof Transactional ? (Transactional) adapter : null;

            try {
                transactionStart(transaction, request);
                response = processor.process(request, workspaceManager, adapter);
                response = response != null ? response : processExtensionRequest(request, adapter);
            } catch (Exception ex) {
                response = handleAdapterException(ex, transaction, request);
            } finally {
                transactionEnd(transaction, request, response);
            }
        } else {
            response = ProviderHelper.notfound(request);
        }

        return response != null ? response : ProviderHelper.badrequest(request);
    }

    private ResponseContext handleAdapterException(Exception ex, Transactional transaction, RequestContext request) {
        if (ex instanceof ResponseContextException) {
            final ResponseContextException rce = (ResponseContextException) ex;

            if (rce.getStatusCode() >= MIN_ERROR_CODE && rce.getStatusCode() < MAX_ERROR_CODE) {
                // don't report routine 4xx HTTP errors
                LOG.info(ex.getMessage(), ex);
            } else {
                LOG.error(ex.getMessage(), ex);
            }
        } else {
            LOG.error(ex.getMessage(), ex);
        }

        transactionCompensate(transaction, request, ex);
        return ProviderHelper.servererror(request, ex);
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

    private ResponseContext processExtensionRequest(RequestContext context, CollectionAdapter adapter) {
        return adapter.extensionRequest(context);
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
