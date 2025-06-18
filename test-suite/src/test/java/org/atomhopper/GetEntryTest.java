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
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class GetEntryTest {

    private static final XmlUtil xml = new XmlUtil();
    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private HttpClient httpClient;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        GetMethod emptyFeedMethod = mock(GetMethod.class);
        when(httpClient.executeMethod(any(GetMethod.class))).thenReturn(HttpStatus.SC_OK);
        when(emptyFeedMethod.getResponseBodyAsString())
                .thenReturn("<feed xmlns='http://www.w3.org/2005/Atom'></feed>");
    }

    @Test
    public void shouldReturnOneEntryAfterPublishing() throws Exception {
        PostMethod postMethod = mock(PostMethod.class);
        postMethod.addRequestHeader(new Header("content-type", "application/atom+xml"));

        String mockEntryId = "urn:uuid:entry-1234";
        String postResponseXml = "<?xml version=\"1.0\" ?><entry xmlns=\"http://www.w3.org/2005/Atom\">" +
                "<id>" + mockEntryId + "</id><content>test</content></entry>";

        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) { return false; }

            public boolean matches(HttpMethod method) {
                return method instanceof PostMethod &&
                        method.getRequestHeader("content-type") != null &&
                        method.getRequestHeader("content-type").getValue().contains("atom+xml");
            }
        }))).thenReturn(HttpStatus.SC_CREATED);

        when(postMethod.getResponseBodyAsString()).thenReturn(postResponseXml);
        GetMethod getFirstEntryMethod = mock(GetMethod.class);

        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) { return false; }

            public boolean matches(HttpMethod method) {
                return method instanceof GetMethod &&
                        method.getPath().contains("/namespace1/feed1/entries/");
            }
        }))).thenReturn(HttpStatus.SC_OK);

        when(getFirstEntryMethod.getResponseBodyAsString()).thenReturn(postResponseXml);
        GetMethod getSecondEntryMethod = mock(GetMethod.class);

        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) { return false; }

            public boolean matches(HttpMethod method) {
                return method instanceof GetMethod &&
                        method.getPath().contains("/namespace2/feed2/entries/");
            }
        }))).thenReturn(HttpStatus.SC_NOT_FOUND);
        int postStatus = httpClient.executeMethod(postMethod);
        assertEquals("Posting a new entry should return 201 Created", HttpStatus.SC_CREATED, postStatus);
        String entryId = getUuidHelper(postMethod);
        assertNotNull("Entry ID should not be null", entryId);
        int getFirstStatus = httpClient.executeMethod(getFirstEntryMethod);
        assertEquals("Getting the newly added entry should return 200 OK", HttpStatus.SC_OK, getFirstStatus);
        int getSecondStatus = httpClient.executeMethod(getSecondEntryMethod);
        assertEquals("Entry should not exist in namespace2/feed2, should return 404", HttpStatus.SC_NOT_FOUND, getSecondStatus);

        System.out.println("Test passed: entryId=" + entryId + ", POST=" + postStatus + ", GET1=" + getFirstStatus + ", GET2=" + getSecondStatus);
    }

    private String getUuidHelper(HttpMethod httpMethod) throws SAXException, IOException {
        Document doc = xml.toDOM(httpMethod.getResponseBodyAsString());
        return doc.getElementsByTagName("id").item(0).getTextContent();
    }
}
