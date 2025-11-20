package org.atomhopper.auth;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.ResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Authorization filter that enforces tenant-based access control
 */
public class TenantAuthorizationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(TenantAuthorizationFilter.class);
    
    private boolean enforceRoleBasedAccess = true;

    public void setEnforceRoleBasedAccess(boolean enforceRoleBasedAccess) {
        this.enforceRoleBasedAccess = enforceRoleBasedAccess;
    }

    @Override
    public ResponseContext filter(RequestContext request, FilterChain chain) {
        // Skip authorization for health checks and version endpoints
        String path = request.getUri().getPath();
        LOG.info("TenantAuthorizationFilter: Processing request to {}", path);
        List<String> pathSegments = extractPathSegments(path);
        String workspaceSegment = !pathSegments.isEmpty() ? pathSegments.get(0) : null;
        String requestedTenant = resolveRequestedTenant(pathSegments);
        
        if (path.endsWith("/health") || path.endsWith("/buildinfo") || path.endsWith("/atommetrics")) {
            LOG.info("TenantAuthorizationFilter: Skipping authorization for system endpoint");
            return chain.next(request);
        }

        String userId = (String) request.getAttribute(RequestContext.Scope.REQUEST, "user.id");
        String userRoles = (String) request.getAttribute(RequestContext.Scope.REQUEST, "user.roles");
        String userTenant = (String) request.getAttribute(RequestContext.Scope.REQUEST, "user.tenant");

        if (userId == null) {
            LOG.warn("No user information found in request context");
            return createForbiddenResponse(request);
        }

        Set<String> normalizedRoles = parseRoles(userRoles);

        // Identity admin must never access identity feeds
        if ("identity".equalsIgnoreCase(workspaceSegment) && normalizedRoles.contains("user-admin")) {
            LOG.warn("Identity user-admin {} denied access to identity feed {}", userId, path);
            return createForbiddenResponse(request);
        }

        if (enforceRoleBasedAccess && requestedTenant != null) {
            if (userTenant == null || !requestedTenant.equalsIgnoreCase(userTenant)) {
                LOG.warn(String.format("User %s attempted to access tenant %s while scoped to %s",
                        userId, requestedTenant, userTenant));
                return createUnauthorizedResponse(request);
            }

            if (!isObserver(normalizedRoles)) {
                LOG.warn(String.format("User %s with roles %s lacks observer access to tenant %s",
                        userId, normalizedRoles, requestedTenant));
                return createForbiddenResponse(request);
            }
        }

        return chain.next(request);
    }

    private boolean isObserver(Set<String> roles) {
        for (String role : roles) {
            if (role.toLowerCase().contains("observer")) {
                return true;
            }
        }
        return false;
    }

    private Set<String> parseRoles(String roles) {
        if (roles == null || roles.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> roleSet = new HashSet<>();
        Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(roleSet::add);
        return roleSet;
    }

    private ResponseContext createForbiddenResponse(RequestContext request) {
        // Create a proper 403 response with XML body
        ResponseContext response = ProviderHelper.forbidden(request, 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
            "  <message>Access denied. Insufficient privileges to access this resource.</message>\n" +
            "</error>");
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        return response;
    }

    private ResponseContext createUnauthorizedResponse(RequestContext request) {
        ResponseContext response = ProviderHelper.unauthorized(request,
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
            "  <message>Invalid tenant scope for this token.</message>\n" +
            "</error>");
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        return response;
    }

    private List<String> extractPathSegments(String path) {
        List<String> segments = new ArrayList<>();
        if (path == null || path.isEmpty()) {
            return segments;
        }
        String[] rawSegments = path.split("/");
        for (String segment : rawSegments) {
            if (segment != null && !segment.isEmpty()) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private String resolveRequestedTenant(List<String> segments) {
        if (segments.isEmpty()) {
            return null;
        }

        for (int i = 0; i < segments.size(); i++) {
            if ("entries".equalsIgnoreCase(segments.get(i)) && i > 0) {
                return segments.get(i - 1);
            }
        }

        return segments.size() >= 3 ? segments.get(2) : null;
    }
}