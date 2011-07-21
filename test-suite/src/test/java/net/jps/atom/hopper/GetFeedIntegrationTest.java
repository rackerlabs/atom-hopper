package net.jps.atom.hopper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
public class GetFeedIntegrationTest extends JettyIntegrationTestHarness {

    public static final HttpClient httpClient = new HttpClient();
    public static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod newGetFeedMethod() {
        return new GetMethod(urlAndPort + "/namespace/feed/");
    }

    public static GetMethod newGetEntryMethod(String entryId) {
        return new GetMethod(urlAndPort + "/namespace/feed/entries/" + entryId);
    }

    public static PostMethod newPostEntryMethod(String entryId) {
        final PostMethod post = new PostMethod(urlAndPort + "/namespace/feed/");
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><id>" + entryId + "</id><content>test</content></entry>");

        return post;
    }

    public static class WhenGettingFeeds {

        @Test
        public void shouldReturnEmptyFeed() throws Exception {
            final HttpMethod getFeedMethod = newGetFeedMethod();
            assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));
            
            System.out.println(new String(getFeedMethod.getResponseBody()));
        }
    }

    public static class WhenGettingFeedsWithMarker {

      @Test
      public void shouldHaveCorrectLinkUrls() throws Exception {
                    
          final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace/feed");
          
          assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));
                     
          Document doc = xml.toDOM(getFeedMethod.getResponseBodyAsString());
          
          assertNotNull("The returned XML should not be null", doc);
          xml.assertHasValue(doc,"/feed/link[@rel='current']/@href", "/namespace/feed");          
          xml.assertHasValue(doc,"/feed/link[@rel='next']/@href", "/namespace/feed?marker=1");          
      }
      
      @Test
      public void shouldPreserveLinkParameters() throws Exception {
          final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace/feed?marker=1&foo=bar");

          assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));

          Document doc = xml.toDOM(getFeedMethod.getResponseBodyAsString());

          assertNotNull("The returned XML should not be null", doc);
          xml.assertHasValue(doc,"/feed/link[@rel='current']/@href", "/namespace/feed?marker=1&foo=bar");
          xml.assertHasValue(doc,"/feed/link[@rel='next']/@href", "/namespace/feed?marker=1&foo=bar");
      }
    }

    public static class WhenPublishingToFeeds {

        @Test
        public void shouldReturnFeedWithOneElementAfterPublishingAnEntry() throws Exception {
            final HttpMethod postMethod = newPostEntryMethod("1");
            assertEquals("Getting a feed should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

            final HttpMethod getEntryMethod = newGetEntryMethod("1");
            assertEquals("Getting a recently added entry should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getEntryMethod));
        }
    }
}
