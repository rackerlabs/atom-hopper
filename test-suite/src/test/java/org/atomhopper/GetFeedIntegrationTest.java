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
import org.xml.sax.SAXException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class GetFeedIntegrationTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod newGetFeedMethod() {
        return new GetMethod(urlAndPort + "/namespace/feed/");
    }

    public static GetMethod newGetEntryMethod(String entryId) {
        return new GetMethod(urlAndPort + "/namespace/feed/entries/" + entryId);
    }

    public static GetMethod newGetEntryWithMarkerMethod(String markerId) {
        return new GetMethod(urlAndPort + "/namespace/feed?marker=" + markerId);
    }

    public static PostMethod newPostEntryMethod(String parameter) {
        final PostMethod post = new PostMethod(urlAndPort + "/namespace/feed/" + parameter);
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><content>test</content></entry>");

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
            XPath xPath = XPathFactory.newInstance().newXPath();
            String linkUrl = xPath.evaluate("/feed/link[@rel='next']/@href", doc);
            String linkUrlOldestEntry = xPath.evaluate("/feed/link[@rel='oldest-entry']/@href", doc);

            assertNotNull("The returned XML should not be null", doc);
            xml.assertHasValue(doc, "/feed/link[@rel='current']/@href", urlAndPort + "/namespace/feed");
            xml.assertHasValue(doc, "/feed/link[@rel='next']/@href", linkUrl);
            xml.assertHasValue(doc, "/feed/link[@rel='oldest-entry']/@href", linkUrlOldestEntry);
        }

        @Test
        public void shouldPreserveAllLinkParameters() throws Exception {
            final HttpMethod getFeedMethod = new GetMethod("http://localhost:24156/namespace/feed?awesome=bar&awesome=foo");

            assertEquals("Getting a feed should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFeedMethod));

            Document doc = xml.toDOM(getFeedMethod.getResponseBodyAsString());
            XPath xPath = XPathFactory.newInstance().newXPath();
            String linkUrl = xPath.evaluate("/feed/link[@rel='next']/@href", doc);
            String linkUrlOldestEntry = xPath.evaluate("/feed/link[@rel='oldest-entry']/@href", doc);
            System.out.println("=================================================");

            xml.assertHasValue(doc, "/feed/link[@rel='current']/@href", "http://localhost:24156/namespace/feed?awesome=bar&awesome=foo");
            xml.assertHasValue(doc, "/feed/link[@rel='next']/@href", linkUrl);
            xml.assertHasValue(doc, "/feed/link[@rel='oldest-entry']/@href", linkUrlOldestEntry);
        }

        @Test
        public void shouldErrorWithBadMarker() throws Exception {

            final HttpMethod getFeedMethod = new GetMethod("http://localhost:24156/namespace/feed?marker=NO_BUENO");

            assertEquals("Getting a feed should return a 500 with bad marker id.", HttpStatus.SC_INTERNAL_SERVER_ERROR, httpClient.executeMethod(getFeedMethod));
        }

        @Test
        public void shouldDefaultToForward() throws Exception {
            final HttpMethod postMethod = newPostEntryMethod("");
            assertEquals("Posting a feed should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));
            String uuid = getUuidHelper(postMethod);
            final HttpMethod getEntryMethod = newGetEntryWithMarkerMethod(uuid);

            assertEquals("Getting an entry with a marker, but missing the direction, should succeed.", HttpStatus.SC_OK, httpClient.executeMethod(getEntryMethod));
        }
    }

    public static class WhenPublishingToFeeds {

        @Test
        public void shouldReturnFeedWithOneElementAfterPublishingAnEntry() throws Exception {
            final HttpMethod postMethod = newPostEntryMethod("");
            assertEquals("Getting a feed should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

            Document doc = xml.toDOM(postMethod.getResponseBodyAsString());
            String uuid = getUuidHelper(postMethod);
            final HttpMethod getEntryMethod = newGetEntryMethod(uuid);

            assertNotNull("The returned XML should not be null", doc);
            assertEquals("Getting a recently added entry should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getEntryMethod));
        }
    }

    private static String getUuidHelper(HttpMethod httpMethod) throws SAXException, IOException {
        return xml.toDOM(httpMethod.getResponseBodyAsString()).getElementsByTagName("id").item(0).getTextContent();
    }
}
