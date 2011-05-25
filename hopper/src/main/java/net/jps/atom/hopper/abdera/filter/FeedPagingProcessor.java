package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import static java.net.URLEncoder.encode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class FeedPagingProcessor implements AdapterResponseProcessor<Feed> {

    public Map<String,String> getParameterMap( RequestContext rc ) {
      Map<String,String> parameters = new HashMap<String,String>();
      for( String parameter: rc.getParameterNames()) {
        parameters.put( parameter, rc.getParameter( parameter ) );
      }

      return parameters;
    }

    public static String mapToString(Map<String,String> parameters) {
      try {
          List<String> result = new ArrayList<String>();

          // Combine the keys into a key=value list
          for( String key : parameters.keySet() ) {
            result.add(encode(key, "UTF-8") + '=' + encode(parameters.get(key), "UTF-8"));
          }

          // Join the list into a string separated by '&'
          return StringUtils.join(result.toArray(),"&");
      }
      catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(e);
      }
    }

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();
        final int numEntries = f.getEntries().size();

        // Get a map of the url parameters
        Map<String,String> parameters = getParameterMap(rc);

        if (numEntries > 0) {
            final Entry first = f.getEntries().get(0), last = f.getEntries().get(numEntries - 1);

            if (first.getId() != null && last.getId() != null) {

                // Build the URL and PATH without the parameters
                final String path = StringUtils.split(rc.getTargetPath(), '?')[0];
                final String self = StringUtils.join(new String[]{StringUtils.chop(rc.getBaseUri().toString()), path});

                // Add current link
                f.addLink(self, "current");

                // Add next link
                parameters.put("marker",last.getId().toString());
                f.addLink(StringUtils.join(new String[]{self, "?", mapToString(parameters)}), "next");

                // Add prev link
                parameters.put("marker",first.getId().toString());
                f.addLink(StringUtils.join(new String[]{self, "?", mapToString(parameters)}), "prev");
            }
        }
    }
}
