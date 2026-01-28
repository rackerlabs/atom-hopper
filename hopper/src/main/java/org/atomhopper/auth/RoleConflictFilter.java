package org.atomhopper.auth;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.ResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight filter that detects conflicting user roles from Repose headers
 * and returns 409 Conflict to prevent publication issues and rate limit problems.
 * 
 * This filter only reads headers set by Repose - it does NOT authenticate users.
 * 
 * Role Categories Checked:
 * - service-admin: Full system access
 * - admin: Tenant admin access
 * - observer: Read-only access
 * - cadf-publisher: CADF event publishing
 * - custom publishers: Schema-level publisher access
 * 
 * When users have multiple role categories, returns 409 Conflict.
 */
public class RoleConflictFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RoleConflictFilter.class);
    
    private static final String X_ROLES = "X-Roles";
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_NAME = "X-User-Name";
    
    private boolean enableRoleConflictCheck = true;

    public void setEnableRoleConflictCheck(boolean enableRoleConflictCheck) {
        this.enableRoleConflictCheck = enableRoleConflictCheck;
    }

    @Override
    public ResponseContext filter(RequestContext request, FilterChain chain) {
        String path = request.getUri().getPath();
        if (path.endsWith("/health") || path.endsWith("/buildinfo") || path.endsWith("/atommetrics")) {
            return chain.next(request);
        }

        if (!enableRoleConflictCheck) {
            return chain.next(request);
        }

        String userId = getHeaderIgnoreCase(request, X_USER_ID);
        if (userId == null) {
            userId = getHeaderIgnoreCase(request, X_USER_NAME);
        }
        String userRoles = getHeaderIgnoreCase(request, X_ROLES);
        
        if (userId == null || userRoles == null) {
            return chain.next(request);
        }

        if (hasRoleConflicts(userRoles)) {
            LOG.warn("User {} has conflicting roles: {} - returning 409 Conflict", userId, userRoles);
            return createConflictResponse(request);
        }

        return chain.next(request);
    }

    /**
     * Checks if user has conflicting role categories.
     * Returns true if user has roles from multiple categories.
     */
    private boolean hasRoleConflicts(String roles) {
        if (roles == null || roles.trim().isEmpty()) {
            return false;
        }

        Set<String> roleSet = new HashSet<>();
        Arrays.stream(roles.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .forEach(roleSet::add);

        boolean hasService = false, hasAdmin = false, hasObserver = false;
        boolean hasCadfPublisher = false, hasCustomPublisher = false;
        
        for (String role : roleSet) {
            if (role.contains("service")) {
                hasService = true;
            } else if (role.contains("admin")) {
                hasAdmin = true;
            } else if (role.contains("observer")) {
                hasObserver = true;
            } else if (role.contains("cadf") && role.contains("publisher")) {
                hasCadfPublisher = true;
            } else if (role.contains("publisher")) {
                hasCustomPublisher = true;
            }
        }

        int roleCategories = 0;
        if (hasService) roleCategories++;
        if (hasAdmin) roleCategories++;
        if (hasObserver) roleCategories++;
        if (hasCadfPublisher) roleCategories++;
        if (hasCustomPublisher) roleCategories++;
        
        if (roleCategories > 1) {
            LOG.debug("Role conflict detected - service: {}, admin: {}, observer: {}, cadf-publisher: {}, custom-publisher: {}", 
                    new Object[]{hasService, hasAdmin, hasObserver, hasCadfPublisher, hasCustomPublisher});
            return true;
        }

        return false;
    }

    private ResponseContext createConflictResponse(RequestContext request) {
        String errorBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
            "  <message>User has conflicting role assignments. Please contact administrator to resolve role conflicts.</message>\n" +
            "</error>";
        
        ResponseContext response = ProviderHelper.conflict(request, errorBody);
        response.setStatus(409);
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        return response;
    }

    private String getHeaderIgnoreCase(RequestContext request, String headerName) {
        if (headerName == null) {
            return null;
        }   
        String value = request.getHeader(headerName);
        if (value != null) {
            return value;
        }
        value = request.getHeader(headerName.toLowerCase());
        if (value != null) {
            return value; 
        }
        return request.getHeader(headerName.toUpperCase());
    }
}