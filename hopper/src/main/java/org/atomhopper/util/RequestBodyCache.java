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
     * Buffers the request body content and stores it in the request attributes
     * 
     * @param request The original RequestContext
     * @return The same RequestContext with buffered body content
     * @throws IOException if there's an error reading the request body
     */
    public static RequestContext buffer(RequestContext request) throws IOException {
        // Check if already buffered
        byte[] cachedBody = (byte[]) request.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        if (cachedBody != null) {
            return request;
        }

        // Read and cache the body
        InputStream inputStream = request.getInputStream();
        if (inputStream == null) {
            cachedBody = new byte[0];
        } else {
            cachedBody = readInputStream(inputStream);
        }

        // Store in request attributes
        request.setAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE, cachedBody);
        
        return request;
    }

    /**
     * Gets the cached body content from a buffered request
     * 
     * @param request The buffered RequestContext
     * @return The cached body content as byte array
     */
    public static byte[] getBody(RequestContext request) {
        byte[] cachedBody = (byte[]) request.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        return cachedBody != null ? cachedBody : new byte[0];
    }

    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        return buffer.toByteArray();
    }
}