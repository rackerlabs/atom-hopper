package org.atomhopper.util.uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Due to some strange restriction placed on the URLStreamHandlerFactory (see:
 * http://download.oracle.com/javase/6/docs/api/index.html?java/net/URL.html) this
 * little mapper was introduced to allow for custom scheme matching.
 */
public class CustomSchemeResolver implements UriToUrlResolver {

    public static UriToUrlResolver newDefaultInstance() {
        final CustomSchemeResolver resolverInstance = new CustomSchemeResolver();
        resolverInstance.addMapper(new ClasspathSchemeMapper());

        return resolverInstance;
    }

    
    private final Set<URISchemeMapper> schemeMapperSet;

    public CustomSchemeResolver() {
        schemeMapperSet = new HashSet<URISchemeMapper>();
    }

    public void addMapper(URISchemeMapper mapper) {
        schemeMapperSet.add(mapper);
    }

    @Override
    public URL toURL(URI uri) throws MalformedURLException {
        for (URISchemeMapper mapper : schemeMapperSet) {
            if (mapper.canMap(uri)) {
                return mapper.toURL(uri);
            }
        }

        //Default fallback
        return uri.toURL();
    }
}
