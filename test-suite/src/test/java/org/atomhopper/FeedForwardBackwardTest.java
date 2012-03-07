package org.atomhopper;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


@RunWith(Enclosed.class)
public class FeedForwardBackwardTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    public static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();
    private static Abdera abdera = null;
    
    public static synchronized Abdera getInstance() {
        if (abdera == null) {
            abdera = new Abdera();
        }
        return abdera;
    }    

    public static PostMethod newPostEntryMethod(String content) {
        final PostMethod post = new PostMethod(getURL());
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><author><name>Chad</name></author><content>" + content + "</content></entry>");

        return post;
    }
    
    public static String getURL() {
        return urlAndPort + "/namespace3/feed3/";
    }
    
    public static GetMethod getFeedMethod() {
        return new GetMethod(getURL());
    }    
    
    public static String getFeedDirectionForwardMethod(String markerId) {
        return getURL() + "?marker=" + markerId + "&direction=forward&limit=10";
    }  
    
    public static String getFeedDirectionBackwardMethod(String markerId) {
        return getURL() + "?marker=" + markerId + "&direction=backward&limit=10";
    } 
    
    public static class WhenRequestingFeed {
        @Test
        public void shouldOrderCorrectlyForwardAndBackward() throws Exception {
            final HttpMethod getFeedMethod = getFeedMethod();
            assertEquals("Hitting Atom Hopper with an empty datastore should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));            
            // Create 20 new entries
            for(int i = 1; i < 21; i++) {
                final HttpMethod postMethod = newPostEntryMethod("<order>" + Integer.toString(i) + "</order>");
                assertEquals("Creating a new entry should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));
            }
            
            // namespace3/feed3
            assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));

            // A bit verbose, but it checks the forward and backward direction of the feed
            Parser parser = getInstance().getParser();
            URL url = new URL(getURL());
            Document<Feed> doc = parser.parse(url.openStream(), url.toString());
            Feed feed = doc.getRoot();
            List<String> idList = new ArrayList<String>();
 
            // Get the IDs in their default order
            for (Entry entry : feed.getEntries()) {
                idList.add(entry.getId().toString());
            }
            
            if(!(idList.isEmpty())) {
                int idCount = 0;
                
                // Check the feed backward with the first id as the marker
                URL urlBackward = new URL(getFeedDirectionBackwardMethod(idList.get(0)));
                Document<Feed> docBackward = parser.parse(urlBackward.openStream(), urlBackward.toString());
                Feed feedBackward = docBackward.getRoot();
                
                for (Entry entry : feedBackward.getEntries()) {
                    assertEquals("The entries should be in backward order", entry.getId().toString(), idList.get(idCount));
                    idCount++;
                }
                
                // Check the feed forward with the last id as the marker
                URL urlForward = new URL(getFeedDirectionForwardMethod(idList.get(idList.size() - 1)));
                Document<Feed> docForward = parser.parse(urlForward.openStream(), urlForward.toString());
                Feed feedForward = docForward.getRoot();
                // Adjust for the offset going forward
                idCount = 9;

                for (Entry entry : feedForward.getEntries()) {
                    assertEquals("The entries should be in forward order", entry.getId().toString(), idList.get(idCount));
                    idCount++;
                }
            }
        }
    }    
}
