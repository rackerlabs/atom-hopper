package org.atomhopper.abdera.filter;

import org.apache.abdera.i18n.text.UrlEncoding;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.response.AdapterResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 *
 */
public class FeedPagingProcessor implements AdapterResponseInterceptor<Feed> {

    private static final String NEXT_LINK = "next";
    private static final String CURRENT_LINK = "current";
    private static final String DIRECTION = "direction";
    private static final String SELF_LINK = "self";

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
        if (linkNotSet(f, CURRENT_LINK)) {
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), CURRENT_LINK);
        }

        // Add self link (same as current link)
        if (linkNotSet(f, SELF_LINK)) {
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), SELF_LINK);
        }

        // If the feed source hasn't already defined this link
        if (linkNotSet(f, NEXT_LINK)) {
            String id = f.getEntries().get(f.getEntries().size() - 1).getId().toString();

            if (parameters.containsKey(DIRECTION)) {
                if (parameters.get(DIRECTION).get(0).equalsIgnoreCase("forward")) {
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
                values.add(value.replace("amp;", ""));
            }
            parameters.put(parameter.toLowerCase().replace("amp;", ""), values);
        }

        return parameters;
    }

    public static String mapToParameters(Map<String, List<String>> parameters) {
        try {
            List<String> result = new ArrayList<String>();
            String queryString = "";

            // make string with params
            // unencode string
            //do everything else
            //re-encode and pass back

            // Combine the keys into a key=value list
            for (String key : parameters.keySet()) {
                //The key isn't unique, and we might end up with an array of multiple parameters
                for (String value : parameters.get(key)) {
                    result.add(key + '=' + value);
                }
            }

            queryString = StringUtils.join(result.toArray(), "&");

            //queryString = encode(queryString, "UTF-8");

            String s = UrlEncoding.decode(queryString);

            if (queryString == null || queryString.isEmpty()) {
                return "";
            }

/*
            for (int i = 0; i < result.size(); i++) {
                queryString += result.get(i);
                if (i + 1 < result.size() && !result.get(i).contains("&amp;") && !result.get(i).contains("amp%3B")) {
                    queryString += "&";
                }
            }
*/

/*
            String s = StringUtils.join(new String[]{"?", StringUtils.join(result.toArray(), "&")});

            // Join the list into a string separated by '&' and prefix with '?'
            return s;
*/

            return "?" + queryString;

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
