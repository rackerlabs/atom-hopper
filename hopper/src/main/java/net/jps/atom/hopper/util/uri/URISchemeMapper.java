package net.jps.atom.hopper.util.uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface URISchemeMapper {

    boolean canMap(URI uriToMatch);

    URL toURL(URI uriToMap) throws MalformedURLException;

}
