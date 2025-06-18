package org.atomhopper;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(Enclosed.class)
public class RegexFeedTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:8083";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnFeedWithOneElementAfterPublishingAnEntry() throws Exception {
        final HttpMethod postMethod = newPostEntryMethod("namespace4/feed4/1", "");
        assertEquals("Posting to a regex feed should return 201 Created", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

        Document doc = xml.toDOM(postMethod.getResponseBodyAsString());
        String uuid1 = getUuidHelper(postMethod);

        final HttpMethod getFirstEntryMethod = getEntryMethod("namespace4/feed4/1", uuid1);
        assertNotNull("The returned XML should not be null", doc);
        assertEquals("Getting a recently added entry should return 200 OK", HttpStatus.SC_OK, httpClient.executeMethod(getFirstEntryMethod));
    }

    @Test
    public void shouldFailToPostEntryOnRegexFeedWithoutEnablingRegexFeed() throws Exception {
        final HttpMethod postMethod = newPostEntryMethod("namespace5/feed5/1", "");
        assertEquals("Posting to a regex feed with regex disabled should return 404 Not Found", HttpStatus.SC_NOT_FOUND, httpClient.executeMethod(postMethod));
    }

    public static GetMethod getEntryMethod(String feedResource, String entryId) {
        return new GetMethod(urlAndPort + "/" + feedResource + "/entries/" + entryId);
    }

    public static PostMethod newPostEntryMethod(String feedResource, String parameter) {
        final PostMethod post = new PostMethod(urlAndPort + "/" + feedResource + parameter);
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><content>test</content></entry>");
        return post;
    }

    private static String getUuidHelper(HttpMethod httpMethod) throws SAXException, IOException {
        return xml.toDOM(httpMethod.getResponseBodyAsString()).getElementsByTagName("id").item(0).getTextContent();
    }
}
