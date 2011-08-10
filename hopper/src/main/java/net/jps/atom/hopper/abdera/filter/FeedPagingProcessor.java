package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.lang.StringUtils;

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
public class FeedPagingProcessor implements AdapterResponseProcessor<Feed> {

    private static final String NEXT_LINK = "next";
    private static final String CURRENT_LINK = "current";

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();

        // Build the URL and PATH without the parameters
        final String self = rc.getBaseUri().toString() + StringUtils.split(rc.getTargetPath(), '?')[0];

        // Get a map of the url parameters
        Map<String,String> parameters = getParameterMap(rc);

        // If there are no entries in the feed
        if (f.getEntries().size() == 0) {
            return;
        }

        // Add current link
        if (linkNotSet(f, CURRENT_LINK)) {
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), CURRENT_LINK);
        }

        // If the feed source hasn't already defined this link
        if (linkNotSet(f, NEXT_LINK)) {
            // Get the id of the last entry on this page
            String id = f.getEntries().get(f.getEntries().size() - 1).getId().toString();
            parameters.put("marker", id);
            f.addLink(StringUtils.join(new String[]{self, mapToParameters(parameters)}), NEXT_LINK);
        }
    }

    private boolean linkNotSet(Feed feed, String link) {
        return (feed.getLinks(link).size() == 0);
    }

    public Map<String,String> getParameterMap( RequestContext rc ) {
      Map<String,String> parameters = new HashMap<String,String>();
      for( String parameter: rc.getParameterNames()) {
        parameters.put( parameter, rc.getParameter( parameter ) );
      }

      return parameters;
    }

    public static String mapToParameters(Map<String,String> parameters) {
      try {
          List<String> result = new ArrayList<String>();

          // Combine the keys into a key=value list
          for( String key : parameters.keySet() ) {
            result.add(encode(key, "UTF-8") + '=' + encode(parameters.get(key), "UTF-8"));
          }

          if(result.isEmpty()){
              return "";
          }

          // Join the list into a string separated by '&' and prefix with '?'
          return StringUtils.join(new String[]{ "?", StringUtils.join(result.toArray(),"&")});
      }
      catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(e);
      }
    }

}
