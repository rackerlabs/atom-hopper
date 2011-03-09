package net.jps.atom.hopper.util.uri;

import com.rackspace.cloud.commons.util.StringUtilities;
import java.net.URI;
import java.net.URL;

public class ClasspathSchemeMapper implements URISchemeMapper {

    @Override
    public boolean canMap(URI uriToMatch) {
        final String uriScheme = uriToMatch.getScheme();

        return !StringUtilities.isBlank(uriScheme)
                ? uriScheme.startsWith("classpath")
                : false;
    }

    @Override
    public URL toURL(URI uri) {
        return getClass().getResource(uri.getPath());
    }
}
