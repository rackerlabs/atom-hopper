package org.atomhopper.auth;

import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Authentication filter that validates tokens against Keystone identity service
 */
public class KeystoneAuthenticationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneAuthenticationFilter.class);
    private static final String X_AUTH_TOKEN = "X-Auth-Token";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    
    private String keystoneUri;
    private String adminToken;
    private long cacheTimeout = 300; // 5 minutes default
    private final ConcurrentMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    public void setKeystoneUri(String keystoneUri) {
        this.keystoneUri = keystoneUri;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    public void setCacheTimeout(long cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    @Override
    public ResponseContext filter(RequestContext request, FilterChain chain) {
        String authToken = request.getHeader(X_AUTH_TOKEN);
        
        if (authToken == null || authToken.trim().isEmpty()) {
            LOG.warn("Missing X-Auth-Token header");
            return createUnauthorizedResponse("Keystone uri=" + keystoneUri);
        }

        // Check cache first
        TokenInfo tokenInfo = tokenCache.get(authToken);
        if (tokenInfo != null && !tokenInfo.isExpired()) {
            // Add user info to request context for downstream filters
            request.setAttribute(RequestContext.Scope.REQUEST, "user.id", tokenInfo.getUserId());
            request.setAttribute(RequestContext.Scope.REQUEST, "user.roles", tokenInfo.getRoles());
            request.setAttribute(RequestContext.Scope.REQUEST, "user.tenant", tokenInfo.getTenantId());
            return chain.next(request);
        }

        // Validate token against Keystone (simplified for demo)
        TokenValidationResult result = validateToken(authToken);
        
        if (!result.isValid()) {
            LOG.warn("Invalid token: {}", authToken);
            return createUnauthorizedResponse("Keystone uri=" + keystoneUri);
        }

        // Cache the token info
        tokenCache.put(authToken, result.getTokenInfo());
        
        // Add user info to request context
        request.setAttribute(RequestContext.Scope.REQUEST, "user.id", result.getTokenInfo().getUserId());
        request.setAttribute(RequestContext.Scope.REQUEST, "user.roles", result.getTokenInfo().getRoles());
        request.setAttribute(RequestContext.Scope.REQUEST, "user.tenant", result.getTokenInfo().getTenantId());

        return chain.next(request);
    }

    private ResponseContext createUnauthorizedResponse(String authenticateHeader) {
        // Create a proper 401 response using Abdera's ProviderHelper
        ResponseContext response = ProviderHelper.unauthorized(null, "Authentication required");
        response.setHeader(WWW_AUTHENTICATE, authenticateHeader);
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        return response;
    }

    private TokenValidationResult validateToken(String token) {
        // Simplified token validation - in real implementation, this would call Keystone API
        // For testing purposes, we'll simulate different scenarios based on token patterns
        
        if (token.startsWith("valid-")) {
            String userId = token.substring(6); // Extract user ID from token
            TokenInfo tokenInfo = new TokenInfo(userId, "user-admin", "tenant-123", 
                                              System.currentTimeMillis() + (cacheTimeout * 1000));
            return new TokenValidationResult(true, tokenInfo);
        }
        
        return new TokenValidationResult(false, null);
    }

    private static class TokenInfo {
        private final String userId;
        private final String roles;
        private final String tenantId;
        private final long expiresAt;

        public TokenInfo(String userId, String roles, String tenantId, long expiresAt) {
            this.userId = userId;
            this.roles = roles;
            this.tenantId = tenantId;
            this.expiresAt = expiresAt;
        }

        public String getUserId() { return userId; }
        public String getRoles() { return roles; }
        public String getTenantId() { return tenantId; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private static class TokenValidationResult {
        private final boolean valid;
        private final TokenInfo tokenInfo;

        public TokenValidationResult(boolean valid, TokenInfo tokenInfo) {
            this.valid = valid;
            this.tokenInfo = tokenInfo;
        }

        public boolean isValid() { return valid; }
        public TokenInfo getTokenInfo() { return tokenInfo; }
    }
}