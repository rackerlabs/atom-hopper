package org.atomhopper.abdera.filter;

import java.util.List;
import org.apache.abdera.ext.json.JSONFilter;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class performs XML -> JSON transformation on Atom Hopper responses.
 * It can be configured to only perform this transformation on selective URIs.
 * If a particular request URI is in the 'allowedURIs' list (only substring
 * check is performed), then the transformation
 * of XML -> JSON will be performed.
 *
 * User: shin4590
 * Date: 7/1/14
 */
@Component
public class SelectiveURIJSONFilter extends JSONFilter {

    @Autowired
    private List<String> allowedURIs;

    @Override
    public ResponseContext filter(RequestContext request, FilterChain chain) {
        if ( uriAllowed(request.getUri()) ) {
            return super.filter(request, chain);
        } else {
            String format = request.getParameter("format");
            if ( format != null && format.equalsIgnoreCase("json") ) {
                return ProviderHelper.badrequest(request, "format=json is not a valid query parameter for this feed");
            } else {
                return chain.next(request);
            }
        }
    }

    public List<String> getAllowedURIs() {
        return allowedURIs;
    }

    public void setAllowedURIs(List<String> allowedURIs) {
        this.allowedURIs = allowedURIs;
    }

    private boolean uriAllowed(IRI uri) {
        if ( allowedURIs == null ) {
            return false;
        }
        String path = uri.getPath();
        for (String allowedPath : allowedURIs) {
            // actual path maybe: /foo/bar/entries/urn:uuid:xxxx, but
            // allowedURIs can just be /foo/bar
            if ( path.startsWith(allowedPath) ) {
                return true;
            }
        }
        return false;
    }
}
