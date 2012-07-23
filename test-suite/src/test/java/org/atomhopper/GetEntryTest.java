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

@RunWith(Enclosed.class)
public class GetEntryTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod getFirstEntryMethod(String entryId) {
        return new GetMethod(urlAndPort + "/namespace1/feed1/entries/" + entryId);
    }

    public static GetMethod getSecondEntryMethod(String entryId) {
        return new GetMethod(urlAndPort + "/namespace2/feed2/entries/" + entryId);
    }

    public static PostMethod newPostEntryMethod(String parameter) {
        final PostMethod post = new PostMethod(urlAndPort + "/namespace1/feed1/" + parameter);
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><content>test</content></entry>");

        return post;
    }

    public static class WhenWorkingWithEntries {

        @Test
        public void shouldReturnOneEntryAfterPublishing() throws Exception {
            final HttpMethod postMethod = newPostEntryMethod("");
            // POST to the first /namespace1/feed1/
            assertEquals("Posting to a feed should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

            Document doc = xml.toDOM(postMethod.getResponseBodyAsString());
            String uuid = getUuidHelper(postMethod);
            final HttpMethod getFirstEntryMethod = getFirstEntryMethod(uuid);

            assertNotNull("The returned XML should not be null", doc);
            assertEquals("Getting a recently added entry should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFirstEntryMethod));

            // Now check to ensure that the new entry id is not available on the secondary /namepsace2/feed2/
            final HttpMethod getSecondEntryMethod = getSecondEntryMethod(uuid);

            assertNotNull("The returned XML should not be null", doc);
            assertEquals("Getting a recently added entry on a different namespace/feed should return 404", HttpStatus.SC_NOT_FOUND, httpClient.executeMethod(getSecondEntryMethod));
        }
    }

    private static String getUuidHelper(HttpMethod httpMethod) throws SAXException, IOException {
        return xml.toDOM(httpMethod.getResponseBodyAsString()).getElementsByTagName("id").item(0).getTextContent();
    }
}
