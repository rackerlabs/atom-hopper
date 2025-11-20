package org.atomhopper.util;

import org.apache.abdera.protocol.server.RequestContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for caching the request body so that filters can safely read and
 * validate the content without consuming the underlying input stream.
 */
public final class RequestBodyCache {

    public static final String CACHED_BODY_ATTRIBUTE = RequestBodyCache.class.getName() + ".BODY";

    private RequestBodyCache() {
    }

    public static RequestContext buffer(RequestContext request) throws IOException {
        if (request instanceof CachedRequestContext) {
            ensureAttributePresent((CachedRequestContext) request);
            return request;
        }

        byte[] body = readAll(request.getInputStream());
        CachedRequestContext cached = new CachedRequestContext(request, body);
        cached.setAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE, body);
        return cached;
    }

    public static byte[] getBody(RequestContext request) throws IOException {
        Object cached = request.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        if (cached instanceof byte[]) {
            return (byte[]) cached;
        }

        RequestContext buffered = buffer(request);
        Object body = buffered.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        return body instanceof byte[] ? (byte[]) body : new byte[0];
    }

    private static void ensureAttributePresent(CachedRequestContext request) {
        Object cached = request.getAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE);
        if (!(cached instanceof byte[])) {
            request.setAttribute(RequestContext.Scope.REQUEST, CACHED_BODY_ATTRIBUTE, request.getBody());
        }
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
}

