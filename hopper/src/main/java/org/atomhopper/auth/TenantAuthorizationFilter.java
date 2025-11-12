package org.atomhopper.auth;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        if (path.endsWith("/health") || path.endsWith("/buildinfo") || path.endsWith("/atommetrics")) {
            LOG.info("TenantAuthorizationFilter: Skipping authorization for system endpoint");
            return chain.next(request);
        }

        String userId = (String) request.getAttribute(RequestContext.Scope.REQUEST, "user.id");
        String userRoles = (String) request.getAttribute(RequestContext.Scope.REQUEST, "user.roles");
        String userTenant = (String) request.getAttribute(RequestContext.Scope.REQUEST, "user.tenant");

        if (userId == null) {
            LOG.warn("No user information found in request context");
            return createForbiddenResponse();
        }

        // Extract tenant from URL path (e.g., /namespace/feed -> namespace is tenant)
        String[] pathSegments = path.split("/");
        String requestedTenant = null;
        
        if (pathSegments.length > 1 && !pathSegments[1].isEmpty()) {
            requestedTenant = pathSegments[1];
        }

        // Check tenant access
        if (enforceRoleBasedAccess && requestedTenant != null) {
            // Special case: identity:user-admin users should be denied access to identity feeds
            if ("identity".equals(requestedTenant) && userRoles != null && userRoles.contains("user-admin")) {
                LOG.warn("Identity user-admin {} denied access to identity feed", userId);
                return createForbiddenResponse();
            }
            
            if (!canAccessTenant(userTenant, userRoles, requestedTenant)) {
                LOG.warn("User {} with tenant {} and roles {} denied access to tenant {}", 
                        new Object[]{userId, userTenant, userRoles, requestedTenant});
                return createForbiddenResponse();
            }
        }

        return chain.next(request);
    }

    private boolean canAccessTenant(String userTenant, String userRoles, String requestedTenant) {
        // Only full admin users (not user-admin) can access any tenant
        if (userRoles != null && userRoles.equals("admin")) {
            return true;
        }

        // Users can only access their own tenant
        return requestedTenant.equals(userTenant);
    }

    private ResponseContext createForbiddenResponse() {
        // Create a proper 403 response using Abdera's ProviderHelper
        ResponseContext response = ProviderHelper.forbidden(null, "Access denied. Insufficient privileges to access this resource.");
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        return response;
    }
}