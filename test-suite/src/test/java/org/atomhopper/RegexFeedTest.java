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
public class RegexFeedTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static GetMethod getEntryMethod(String feedResource, String entryId) {
        return new GetMethod(urlAndPort + "/" + feedResource + "/entries/" + entryId);
    }

   
    public static PostMethod newPostEntryMethod(String feedResource, String parameter) {
        final PostMethod post = new PostMethod(urlAndPort + "/" + feedResource + parameter);
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><content>test</content></entry>");

        return post;
    }
   
    public static class WhenPublishingToRegexFeeds {

        @Test
        public void shouldReturnFeedWithOneElementAfterPublishingAnEntry() throws Exception {
        	
        	//post an entry to regex feed /namespace4/feed4/1
            final HttpMethod postMethod = newPostEntryMethod("namespace4/feed4/1" ,"");
            // POST to the first namespace4/feed4/1
            assertEquals("Getting a feed should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));

            Document doc = xml.toDOM(postMethod.getResponseBodyAsString());
            String uuid1 = getUuidHelper(postMethod);
            final HttpMethod getFirstEntryMethod = getEntryMethod("namespace4/feed4/1", uuid1);

            assertNotNull("The returned XML should not be null", doc);
            assertEquals("Getting a recently added entry should return a 200", HttpStatus.SC_OK, httpClient.executeMethod(getFirstEntryMethod));
         }
        
       @Test
       public void shouldFailToPostEntryOnRegexFeedWithoutEnablingRegexFeed() throws Exception {
    	   //post an entry to regex feed /namespace5/feed5/1
           final HttpMethod postMethod = newPostEntryMethod("namespace4/feed5/1" ,"");
           // POST to the first namespace4/feed4/1
           assertEquals("Getting a feed should return a 404", HttpStatus.SC_NOT_FOUND, httpClient.executeMethod(postMethod));
       }
    }

    private static String getUuidHelper(HttpMethod httpMethod) throws SAXException, IOException {
        return xml.toDOM(httpMethod.getResponseBodyAsString()).getElementsByTagName("id").item(0).getTextContent();
    }
}
