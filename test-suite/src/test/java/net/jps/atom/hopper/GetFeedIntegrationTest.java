package net.jps.atom.hopper;

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

@RunWith(Enclosed.class)
public class GetFeedIntegrationTest extends JettyIntegrationTestHarness {

    public static final HttpClient httpClient = new HttpClient();

    public static GetMethod newGetFeedMethod() {
        return new GetMethod("http://localhost:" + getPort() + "/namespace/feed/");
    }

    public static GetMethod newGetFeedMethodWithMarker() {
      return new GetMethod("http://localhost:" + getPort() + "/namespace/feed?marker=1");
    }

    public static GetMethod newGetEntryMethod(String entryId) {
        return new GetMethod("http://localhost:" + getPort() + "/namespace/feed/entries/" + entryId);
    }

    public static PostMethod newPostEntryMethod(String entryId) {
        final PostMethod post = new PostMethod("http://localhost:" + getPort() + "/namespace/feed/");
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
      public void shouldReturnEmptyFeed() throws Exception {
        final HttpMethod getFeedMethod = newGetFeedMethodWithMarker();
        assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));

        System.out.println(new String(getFeedMethod.getResponseBody()));
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
