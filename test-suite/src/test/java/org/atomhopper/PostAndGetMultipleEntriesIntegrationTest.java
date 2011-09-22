package org.atomhopper;


import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@RunWith(Enclosed.class)
public class PostAndGetMultipleEntriesIntegrationTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    public static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod getFeedMethod() {
        return new GetMethod(urlAndPort + "/namespace1/feed1/");
    }
    
    public static GetMethod getMarkerFeedMethod(String markerId) {
        return new GetMethod(urlAndPort + "/namespace1/feed1/?marker=" + markerId + "&limit=5");
    }    

    public static GetMethod getEntryMethod(String entryId) {
        return new GetMethod(urlAndPort + "/namespace1/feed1/entries/" + entryId);
    }

    public static PostMethod newPostEntryMethod(String content) {
        final PostMethod post = new PostMethod(urlAndPort + "/namespace1/feed1/");
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><author><name>Chad</name></author><content>" + content + "</content></entry>");

        return post;
    }    

    public static class WhenPublishingMultipleEntries {

        @Test
        public void shouldCreateAndGetMultipleEntries() throws Exception {
            final HttpMethod getFeedMethod = getFeedMethod();
            assertEquals("Hitting Atom Hopper with an empty datastore should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));            
            // Create 15 new entries
            for(int i = 200; i < 216; i++) {
                final HttpMethod postMethod = newPostEntryMethod("<blah><a1>a1</a1><b1>" + Integer.toString(i) + "</b1></blah>");
                assertEquals("Creating a new entry should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));
            }
            
            assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));
            //System.out.println(new String(getFeedMethod.getResponseBody()));
            
            assertEquals("Getting the new feed should show that <b1>215</b1> was added", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));
            assertTrue(new String(getFeedMethod.getResponseBody()).contains("<b1>215</b1>"));
        }
    }    
}
