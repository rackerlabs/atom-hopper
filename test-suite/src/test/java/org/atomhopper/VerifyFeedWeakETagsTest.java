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
public class VerifyFeedWeakETagsTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static PostMethod newCategoryPostEntryMethod(String uri) {
        final PostMethod post = new PostMethod(urlAndPort + uri);
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><content>test</content></entry>");

        return post;
    }

    public static class WhenGettingFeedsWithCategories {

        @Test
        public void shouldReturnANewEntryWithAnETag() throws Exception {  
            final HttpMethod postMethod = newCategoryPostEntryMethod("/namespace/feed/");
            assertEquals("Creating a new entry should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

            final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace/feed/");
            httpClient.executeMethod(getFeedMethod);
            assertTrue("The returned feed should set an etag in the header", getFeedMethod.getResponseHeader("ETag").getValue().contains("W/"));
        }
    }

    public static class WhenGettingRoleAwareFeeds {
        @Test
        public void shouldReturnANewEntryWithETagRole() throws Exception {
            final HttpMethod postMethod = newCategoryPostEntryMethod("/namespace6/feed6/");
            assertEquals("Creating a new entry should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

            final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace6/feed6/");
            getFeedMethod.addRequestHeader("x-access", "level1");
            httpClient.executeMethod(getFeedMethod);
            String etagL1 = "";
            if(null != getFeedMethod.getResponseHeader("ETag")) {
            	etagL1 = getFeedMethod.getResponseHeader("ETag").getValue();
                assertTrue("The returned feed should set an etag in the header", etagL1.contains("W/"));
            }
            

            final HttpMethod getFeedMethodAsLevel3 = new GetMethod(urlAndPort + "/namespace6/feed6");
            getFeedMethodAsLevel3.addRequestHeader("x-access", "level3");
            httpClient.executeMethod(getFeedMethodAsLevel3);
            final String etagL3 = getFeedMethodAsLevel3.getResponseHeader("ETag").getValue();
            assertTrue("The returned feed should set an etag in the header", etagL3.contains("W/"));
            assertTrue("The returned feed should set a different etag header (etag1=" + etagL1 + ", etag3=" + etagL3+")", !etagL1.equals(etagL3));
        }
    }
}
