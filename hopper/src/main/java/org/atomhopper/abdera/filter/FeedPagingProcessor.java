package org.atomhopper.abdera.filter;

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.response.AdapterResponse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.URLEncoder.encode;

/**
 *
 *
 */
public class FeedPagingProcessor implements AdapterResponseInterceptor<Feed> {

    private static final String NEXT_LINK = "next";
    private static final String CURRENT_LINK = "current";
    private static final String DIRECTION = "direction";

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();

        // If there are no entries in the feed
        if (f == null || f.getEntries() == null || f.getEntries().isEmpty()) {
            return;
        }

        // Build the URL and PATH without the parameters
        final String self = StringUtils.split(rc.getResolvedUri().toString(), '?')[0];

        // Get a map of the url parameters
        final Map<String, List<String>> parameters = getParameterMap(rc);

        // Add current link
        if (linkNotSet(f, CURRENT_LINK)) {
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), CURRENT_LINK);
        }

        // If the feed source hasn't already defined this link
        if (linkNotSet(f, NEXT_LINK)) {
            String id = f.getEntries().get(f.getEntries().size() - 1).getId().toString();
            
            if (parameters.containsKey(DIRECTION)) {
                if(parameters.get(DIRECTION).get(0).equalsIgnoreCase("forward")) {
                    id = f.getEntries().get(0).getId().toString();
                }
            }
            List<String> markerList = new ArrayList<String>();
            markerList.add(id);
            parameters.put("marker", markerList);
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), NEXT_LINK);
        }
    }

    private boolean linkNotSet(Feed feed, String link) {
        return feed.getLinks(link).isEmpty();
    }

    public Map<String, List<String>> getParameterMap(RequestContext rc) {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        for (String parameter : rc.getParameterNames()) {
            ArrayList<String> values = new ArrayList<String>();
            for (String value : rc.getParameters(parameter)) {
                values.add(value);
            }
            parameters.put(parameter.toLowerCase(), values);
        }

        return parameters;
    }

    public static String mapToParameters(Map<String, List<String>> parameters) {
        try {
            List<String> result = new ArrayList<String>();

            // Combine the keys into a key=value list
            for (String key : parameters.keySet()) {
                //The key isn't unique, and we might end up with an array of multiple parameters
                for (String value : parameters.get(key)) {
                    result.add(encode(key, "UTF-8") + '=' + encode(value, "UTF-8"));
                }
            }

            if (result.isEmpty()) {
                return "";
            }

            // Join the list into a string separated by '&' and prefix with '?'
            return StringUtils.join(new String[]{"?", StringUtils.join(result.toArray(), "&")});
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
