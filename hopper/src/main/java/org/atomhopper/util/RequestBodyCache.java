package org.atomhopper.util;

import org.apache.abdera.protocol.server.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for caching request body content to allow multiple reads
 */
public class RequestBodyCache {

    private static final Logger LOG = LoggerFactory.getLogger(RequestBodyCache.class);
    private static final String CACHED_BODY_ATTRIBUTE = "cached.request.body";

    /**
     * Buffers the request body content and returns a CachedRequestContext that allows multiple reads
     * 
     * @param request The original RequestContext
     * @return A CachedRequestContext with buffered body content that can be read multiple times
     * @throws IOException if there's an error reading the request body
     */
    public static RequestContext buffer(RequestContext request) throws IOException {
        // Check if already a CachedRequestContext
        if (request instanceof CachedRequestContext) {
            return request;
        }
        
        // Check if already buffered in attributes (for backward compatibility)
        byte[] cachedBody = (byte[]) request.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        if (cachedBody != null) {
            return new CachedRequestContext(request, cachedBody);
        }

        // Read and cache the body
        InputStream inputStream = request.getInputStream();
        if (inputStream == null) {
            cachedBody = new byte[0];
        } else {
            cachedBody = readInputStream(inputStream);
        }

        // Store in request attributes for backward compatibility
        request.setAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE, cachedBody);
        
        // Return a CachedRequestContext that properly overrides getInputStream()
        return new CachedRequestContext(request, cachedBody);
    }

    /**
     * Gets the cached body content from a buffered request
     * 
     * @param request The buffered RequestContext (should be a CachedRequestContext)
     * @return The cached body content as byte array
     */
    public static byte[] getBody(RequestContext request) {
        // First try to get from CachedRequestContext
        if (request instanceof CachedRequestContext) {
            return ((CachedRequestContext) request).getBody();
        }
        
        // Fallback to attributes for backward compatibility
        byte[] cachedBody = (byte[]) request.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        return cachedBody != null ? cachedBody : new byte[0];
    }

    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            LOG.warn("InputStream is null, returning empty byte array");
            return new byte[0];
        }
        
        // Use try-with-resources to ensure proper resource management
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int bytesRead;
            int totalBytesRead = 0;
            
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            
            if (totalBytesRead == 0) {
                LOG.warn("No bytes read from InputStream, content may be empty");
            } else {
                LOG.debug("Successfully read {} bytes from InputStream", totalBytesRead);
            }
            
            return buffer.toByteArray();
        }
        // Note: We don't close the original inputStream here as it's managed by the servlet container
        // and may need to be available for other operations
    }
}