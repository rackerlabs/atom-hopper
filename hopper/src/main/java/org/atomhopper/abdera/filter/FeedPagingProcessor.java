package org.atomhopper.abdera.filter;

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.response.AdapterResponse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.net.URLEncoder.encode;
import org.apache.abdera.model.Link;

/**
 *
 *
 */
public class FeedPagingProcessor implements AdapterResponseInterceptor<Feed> {

    private static final String DIRECTION = "direction";
    private static final String MARKER = "marker";
    private static final String FORWARD = "forward";
    private static final String AMP = "amp;";
    private static final String EMPTY_STRING = "";
    private static final String UTF8 = "UTF-8";

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();

        // If there are no entries in the feed
        if (f == null || f.getEntries() == null || f.getEntries().isEmpty()) {
            return;
        }

        // Build the URL and PATH without the parameters
        final String self = StringUtils.split(rc.getResolvedUri().toString(), '?')[0];
        // Add an updated element to the feed
        final Calendar localNow = Calendar.getInstance(TimeZone.getDefault());
        localNow.setTimeInMillis(System.currentTimeMillis());
        f.setUpdated(localNow.getTime());

        // Get a map of the url parameters
        final Map<String, List<String>> parameters = getParameterMap(rc);

        // Add current link
        if (linkNotSet(f, Link.REL_CURRENT)) {
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), Link.REL_CURRENT);
        }

        // Add self link (same as current link)
        if (linkNotSet(f, Link.REL_SELF)) {
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), Link.REL_SELF);
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
                values.add(value.replace(AMP, EMPTY_STRING));
            }
            parameters.put(parameter.toLowerCase().replace(AMP, EMPTY_STRING), values);
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
                    result.add(encode(key, UTF8) + '=' + encode(value, UTF8));
                }
            }

            String queryString = StringUtils.join(result.toArray(), "&");

            if (queryString == null || queryString.isEmpty()) {
                return EMPTY_STRING;
            }

            queryString = "?" + queryString;

            return queryString;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
