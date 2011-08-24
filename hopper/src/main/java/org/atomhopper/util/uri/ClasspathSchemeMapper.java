package org.atomhopper.util.uri;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URL;

public class ClasspathSchemeMapper implements URISchemeMapper {

    @Override
    public boolean canMap(URI uriToMatch) {
        final String uriScheme = uriToMatch.getScheme();

        return !StringUtils.isBlank(uriScheme)
                ? uriScheme.startsWith("classpath")
                : false;
    }

    @Override
    public URL toURL(URI uri) {
        return getClass().getResource(uri.getPath());
    }
}
