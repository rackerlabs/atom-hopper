package org.atomhopper.util;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.context.RequestContextWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * RequestContext wrapper that replays a cached request body so downstream
 * filters and adapters can read the content even after validation filters
 * have consumed the original input stream.
 */
public class CachedRequestContext extends RequestContextWrapper {

    private final byte[] body;

    public CachedRequestContext(RequestContext request, byte[] body) {
        super(request);
        this.body = body != null ? body : new byte[0];
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body);
    }

    @Override
    public Reader getReader() throws IOException {
        return new InputStreamReader(getInputStream(), StandardCharsets.UTF_8);
    }
}

