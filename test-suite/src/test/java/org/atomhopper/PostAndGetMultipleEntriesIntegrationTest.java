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
import org.w3c.dom.Document;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(Enclosed.class)
public class PostAndGetMultipleEntriesIntegrationTest extends JettyIntegrationTestHarness {

    public static final HttpClient httpClient = new HttpClient();
    public static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod getFeedMethod() {
        return new GetMethod(urlAndPort + "/namespace/feed/");
    }
    
    public static GetMethod getMarkerFeedMethod(String markerId) {
        return new GetMethod(urlAndPort + "/namespace/feed/?marker=" + markerId + "&limit=5");
    }    

    public static GetMethod getEntryMethod(String entryId) {
        return new GetMethod(urlAndPort + "/namespace/feed/entries/" + entryId);
    }

    public static PostMethod newPostEntryMethod(String entryId, String content) {
        final PostMethod post = new PostMethod(urlAndPort + "/namespace/feed/");
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><id>" + entryId + "</id><author><name>Chad</name></author><content>" + content + "</content></entry>");

        return post;
    }

    public static class WhenPublishingMultipleEntries {

        @Test
        public void shouldReturnPartialFeed() throws Exception {
            // Create 15 new entries
            for(int i = 200; i < 216; i++) {
                final HttpMethod postMethod = newPostEntryMethod(Integer.toString(i), "<blah><a1>a1</a1><b1>b1</b1></blah>");
                assertEquals("Creating a new entry should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));
            }
            
            final HttpMethod getFeedMethod = getFeedMethod();
            assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));
            System.out.println(new String(getFeedMethod.getResponseBody()));
            
            final HttpMethod getMarkerFeedMethod = getMarkerFeedMethod("205");
            assertEquals("Getting a recently added entry should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getMarkerFeedMethod));
            System.out.println(new String(getFeedMethod.getResponseBody()));
        }
    }    
}