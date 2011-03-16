package net.jps.atom.hopper;

import org.junit.Ignore;
import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class GetFeedIntegrationTest extends JettyIntegrationTestHarness {

    public static GetMethod newGetFeedMethod() {
        return new GetMethod("http://localhost:" + getPort() + "/namespace/feed/");
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

        final HttpClient c = new HttpClient();

        @Test @Ignore
        public void shouldReturnEmptyFeed() throws Exception {
            final HttpMethod getFeedMethod = newGetFeedMethod();
            assertEquals("Getting a feed should return a 200", HttpStatusCode.OK.intValue(), c.executeMethod(getFeedMethod));
//
//            System.out.println(new String(getMethod.getResponseBody()));
        }

        @Test
        public void shouldReturnFeedWithOneElementAfterPublishingAnEntry() throws Exception {
            final HttpMethod postMethod = newPostEntryMethod("1");
            assertEquals("Getting a feed should return a 201", HttpStatusCode.CREATED.intValue(), c.executeMethod(postMethod));

            final HttpMethod getEntryMethod = newGetEntryMethod("1");
            assertEquals("Getting a recently added entry should return a 200", HttpStatusCode.OK.intValue(), c.executeMethod(getEntryMethod));
        }
    }
}
