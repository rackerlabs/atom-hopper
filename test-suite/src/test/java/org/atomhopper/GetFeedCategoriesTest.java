package org.atomhopper;

import org.w3c.dom.NodeList;
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
import org.w3c.dom.Element;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


@RunWith(Enclosed.class)
public class GetFeedCategoriesTest extends JettyIntegrationTestHarness {

    private static final HttpClient httpClient = new HttpClient();
    private static final XmlUtil xml = new XmlUtil();
    private static final String urlAndPort = "http://localhost:" + getPort();

    public static PostMethod newCategoryPostEntryMethod(String term) {
        final PostMethod post = new PostMethod(urlAndPort + "/namespace/feed/");
        post.addRequestHeader(new Header("content-type", "application/atom+xml"));
        post.setRequestBody("<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\"><category term=\"" + term + "\" /><content>test</content></entry>");

        return post;
    }

    public static class WhenGettingFeedsWithCatrgories {

        @Test
        public void shouldCorrectlyInsertAndRetrieveEntriesWithCategories() throws Exception {        
            // Create 3 new entries with categories
            final String[] categories = {"cat1", "cat2", "cat3"};
            for (String term : categories) {
                final HttpMethod postMethod = newCategoryPostEntryMethod(term);
                assertEquals("Creating a new entry should return a 201", HttpStatus.SC_CREATED, httpClient.executeMethod(postMethod));
                
                final HttpMethod getFeedMethod = new GetMethod(urlAndPort + "/namespace/feed/?search=%2B" + term);
                httpClient.executeMethod(getFeedMethod);
                Document doc = xml.toDOM(getFeedMethod.getResponseBodyAsString());
                assertNotNull("The returned XML should not be null", doc);
                
                NodeList nodeList = doc.getElementsByTagName("category");
                
                for( int i = 0; i < nodeList.getLength(); i++ ) {
                  // For every category tag
                  Element categoryElement = (Element) nodeList.item(i);
                  assertTrue(categoryElement.hasAttribute("term"));
                  assertEquals(categoryElement.getAttribute("term"), term);
                }        
            }
        }
    }
}
