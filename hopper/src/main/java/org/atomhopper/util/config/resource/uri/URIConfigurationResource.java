package org.atomhopper.util.config.resource.uri;

import org.atomhopper.util.config.resource.ConfigurationResource;
import org.atomhopper.util.config.resource.ConfigurationResourceException;
import org.atomhopper.util.uri.CustomSchemeResolver;
import org.atomhopper.util.uri.UriToUrlResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URIConfigurationResource implements ConfigurationResource {

    private final URI resourceLocation;
    private final UriToUrlResolver uriSchemeResolver;

    public URIConfigurationResource(URI resourceLocation) {
        this(resourceLocation, CustomSchemeResolver.newDefaultInstance());
    }

    public URIConfigurationResource(URI resourceLocation, UriToUrlResolver customUriSchemeResolver) {
        this.resourceLocation = resourceLocation;
        this.uriSchemeResolver = customUriSchemeResolver;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            final URL location = uriSchemeResolver.toURL(resourceLocation);
            return location.openStream();
        } catch (MalformedURLException murle) {
            throw new ConfigurationResourceException("URI produces a malformed URL. URI, \""
                    + resourceLocation.toString()
                    + "\" may be unsupported. Reason: " + murle.getMessage(), murle);
        }
    }
}
