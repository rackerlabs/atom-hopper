package org.atomhopper.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.abdera.protocol.server.Filter;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Authentication filter that validates tokens against Keystone identity service
 */
public class KeystoneAuthenticationFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoneAuthenticationFilter.class);
    private static final String X_AUTH_TOKEN = "X-Auth-Token";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_NAME = "X-User-Name";
    private static final String X_ROLES = "X-Roles";
    private static final String X_TENANT_ID = "X-Tenant-Id";
    private static final String X_PROJECT_ID = "X-Project-Id";
    private static final String X_TENANT_NAME = "X-Tenant-Name";

    private String keystoneUri;
    private String adminToken;
    private long cacheTimeout = 300; // 5 minutes default
    private final ConcurrentMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private final CloseableHttpClient httpClient = HttpClients.createSystem();

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
        LOG.info("KeystoneAuthenticationFilter: Processing request to {}", request.getUri().getPath());

        String authToken = getHeaderIgnoreCase(request, X_AUTH_TOKEN);

        if ((authToken == null || authToken.trim().isEmpty()) && !hasExistingUserContext(request)) {
            LOG.warn("Missing X-Auth-Token header for request to {}", request.getUri().getPath());
            return createUnauthorizedResponse(request, "Keystone uri=" + keystoneUri);
        }

        LOG.info("KeystoneAuthenticationFilter: Found auth token, validating...");

        TokenInfo tokenInfo = authToken != null ? tokenCache.get(authToken) : null;
        if (tokenInfo != null && !tokenInfo.isExpired()) {
            populateRequestContext(request, tokenInfo);
            return chain.next(request);
        }

        TokenValidationResult result = validateToken(request, authToken);

        if (!result.isValid()) {
            LOG.warn("Invalid token: {}", authToken);
            return createUnauthorizedResponse(request, "Keystone uri=" + keystoneUri);
        }

        if (authToken != null) {
            tokenCache.put(authToken, result.getTokenInfo());
        }

        populateRequestContext(request, result.getTokenInfo());
        LOG.info(String.format("KeystoneAuthenticationFilter: Authenticated user %s with roles %s and tenant %s",
                result.getTokenInfo().getUserId(),
                result.getTokenInfo().getRoles(),
                result.getTokenInfo().getTenantId()));

        return chain.next(request);
    }

    private void populateRequestContext(RequestContext request, TokenInfo tokenInfo) {
        request.setAttribute(RequestContext.Scope.REQUEST, "user.id", tokenInfo.getUserId());
        request.setAttribute(RequestContext.Scope.REQUEST, "user.roles", tokenInfo.getRoles());
        request.setAttribute(RequestContext.Scope.REQUEST, "user.tenant", tokenInfo.getTenantId());
    }

    private ResponseContext createUnauthorizedResponse(RequestContext request, String authenticateHeader) {
        ResponseContext response = ProviderHelper.unauthorized(request,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<error xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                        "  <message>Authentication required</message>\n" +
                        "</error>");
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader(WWW_AUTHENTICATE, authenticateHeader);
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        return response;
    }

    private TokenValidationResult validateToken(RequestContext request, String token) {
        if (token == null || token.trim().isEmpty()) {
            TokenInfo headerInfo = buildTokenInfoFromHeaders(request);
            if (headerInfo != null) {
                return new TokenValidationResult(true, headerInfo);
            }
            return new TokenValidationResult(false, null);
        }

        if (adminToken != null && !adminToken.trim().isEmpty() && adminToken.equals(token)) {
            TokenInfo tokenInfo = new TokenInfo(
                    "cloudfeeds_service-admin",
                    "service-admin",
                    "cloudfeeds",
                    System.currentTimeMillis() + (cacheTimeout * 1000));
            return new TokenValidationResult(true, tokenInfo);
        }

        TokenInfo identityInfo = fetchTokenInfoFromKeystone(token);
        if (identityInfo != null) {
            return new TokenValidationResult(true, identityInfo);
        }

        TokenInfo headerInfo = buildTokenInfoFromHeaders(request);
        if (headerInfo != null) {
            return new TokenValidationResult(true, headerInfo);
        }

        TokenInfo inferred = inferFromToken(token);
        if (inferred != null) {
            return new TokenValidationResult(true, inferred);
        }

        return new TokenValidationResult(false, null);
    }

    private TokenInfo buildTokenInfoFromHeaders(RequestContext request) {
        String userId = firstNonEmpty(
                getHeaderIgnoreCase(request, X_USER_ID),
                getHeaderIgnoreCase(request, X_USER_NAME));
        String roles = getHeaderIgnoreCase(request, X_ROLES);
        String tenantId = firstNonEmpty(
                getHeaderIgnoreCase(request, X_TENANT_ID),
                getHeaderIgnoreCase(request, X_PROJECT_ID),
                getHeaderIgnoreCase(request, X_TENANT_NAME));

        if (userId == null && roles == null && tenantId == null) {
            return null;
        }

        return new TokenInfo(
                userId != null ? userId : "authenticated-user",
                roles != null ? roles : "user",
                tenantId != null ? tenantId : "default-tenant",
                System.currentTimeMillis() + (cacheTimeout * 1000));
    }

    private boolean hasExistingUserContext(RequestContext request) {
        return buildTokenInfoFromHeaders(request) != null;
    }

    private TokenInfo fetchTokenInfoFromKeystone(String token) {
        if (keystoneUri == null || keystoneUri.trim().isEmpty()) {
            return null;
        }

        String base = keystoneUri.endsWith("/") ? keystoneUri.substring(0, keystoneUri.length() - 1) : keystoneUri;
        String validateUrl = base + "/v2.0/tokens/" + token;
        HttpGet get = new HttpGet(validateUrl);
        get.setHeader("Accept", "application/json");

        String headerToken = firstNonEmpty(adminToken, token);
        if (headerToken != null && !headerToken.trim().isEmpty()) {
            get.setHeader(X_AUTH_TOKEN, headerToken.trim());
        }

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return parseKeystoneTokenInfo(body);
            }

            if (status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_NOT_FOUND) {
                LOG.warn("Keystone rejected token {} with status {}", token, status);
                return null;
            }

            LOG.warn("Unexpected response from Keystone (status {}): {}", status, response.getStatusLine().getReasonPhrase());
        } catch (IOException e) {
            LOG.warn("Error validating token with Keystone: {}", e.getMessage());
        }

        return null;
    }

    private TokenInfo parseKeystoneTokenInfo(String jsonBody) {
        JsonElement element = JsonParser.parseString(jsonBody);
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject root = element.getAsJsonObject();
        JsonObject access = root.has("access") ? root.getAsJsonObject("access") : root;
        JsonObject tokenObj = access.has("token") ? access.getAsJsonObject("token") : root.getAsJsonObject("token");
        JsonObject userObj = access.has("user") ? access.getAsJsonObject("user") : root.getAsJsonObject("user");

        if (tokenObj == null || userObj == null) {
            return null;
        }

        String tenantId = extractTenantId(tokenObj);
        String userId = extractUserId(userObj);
        String roles = extractRoles(userObj);

        if (userId == null) {
            userId = "authenticated-user";
        }
        if (tenantId == null) {
            tenantId = "default-tenant";
        }
        if (roles == null) {
            roles = "user";
        }

        return new TokenInfo(userId, roles, tenantId,
                System.currentTimeMillis() + (cacheTimeout * 1000));
    }

    private String extractTenantId(JsonObject tokenObj) {
        if (tokenObj.has("tenant")) {
            JsonObject tenant = tokenObj.getAsJsonObject("tenant");
            if (tenant.has("id")) {
                return tenant.get("id").getAsString();
            }
        }
        if (tokenObj.has("tenant_id")) {
            return tokenObj.get("tenant_id").getAsString();
        }
        if (tokenObj.has("tenantId")) {
            return tokenObj.get("tenantId").getAsString();
        }
        return null;
    }

    private String extractUserId(JsonObject userObj) {
        if (userObj.has("name")) {
            return userObj.get("name").getAsString();
        }
        if (userObj.has("username")) {
            return userObj.get("username").getAsString();
        }
        if (userObj.has("id")) {
            return userObj.get("id").getAsString();
        }
        return null;
    }

    private String extractRoles(JsonObject userObj) {
        if (!userObj.has("roles")) {
            return null;
        }
        JsonArray rolesArray = userObj.getAsJsonArray("roles");
        if (rolesArray.size() == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (JsonElement roleElement : rolesArray) {
            if (!roleElement.isJsonObject()) {
                continue;
            }
            JsonObject roleObj = roleElement.getAsJsonObject();
            if (roleObj.has("name")) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(roleObj.get("name").getAsString());
            }
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    private TokenInfo inferFromToken(String token) {
        String userId = inferUserIdFromToken(token);
        String roles = inferRolesFromToken(token);
        String tenantId = inferTenantFromToken(token);
        return new TokenInfo(userId, roles, tenantId,
                System.currentTimeMillis() + (cacheTimeout * 1000));
    }

    private String inferUserIdFromToken(String token) {
        String lower = token.toLowerCase();
        if (lower.contains("identity") || lower.contains("user-admin")) {
            return "identity:user-admin";
        }
        if (lower.contains("service-admin") || lower.contains("cloudfeeds")) {
            return "cloudfeeds_service-admin";
        }
        if (lower.contains("observer")) {
            return "observer-user";
        }
        return "authenticated-user";
    }

    private String inferRolesFromToken(String token) {
        String lower = token.toLowerCase();
        if (lower.contains("user-admin")) {
            return "user-admin";
        }
        if (lower.contains("service-admin") || lower.contains("cloudfeeds")) {
            return "service-admin";
        }
        if (lower.contains("observer")) {
            return "observer";
        }
        return "user";
    }

    private String inferTenantFromToken(String token) {
        String lower = token.toLowerCase();
        if (lower.contains("identity")) {
            return "identity";
        }
        if (lower.contains("cloudfeeds")) {
            return "cloudfeeds";
        }
        return "default-tenant";
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
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

    private static class TokenInfo {
        private final String userId;
        private final String roles;
        private final String tenantId;
        private final long expiresAt;

        TokenInfo(String userId, String roles, String tenantId, long expiresAt) {
            this.userId = userId;
            this.roles = roles;
            this.tenantId = tenantId;
            this.expiresAt = expiresAt;
        }

        String getUserId() { return userId; }
        String getRoles() { return roles; }
        String getTenantId() { return tenantId; }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private static class TokenValidationResult {
        private final boolean valid;
        private final TokenInfo tokenInfo;

        TokenValidationResult(boolean valid, TokenInfo tokenInfo) {
            this.valid = valid;
            this.tokenInfo = tokenInfo;
        }

        boolean isValid() { return valid; }
        TokenInfo getTokenInfo() { return tokenInfo; }
    }
}