package net.jps.atom.hopper.util.uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface UriToUrlResolver {

    URL toURL(URI uri) throws MalformedURLException;

}
