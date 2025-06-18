package org.atomhopper;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
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

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class FeedForwardBackwardTest extends JettyIntegrationTestHarness {

    @Mock
    private HttpClient httpClient;

    private final Abdera abdera = new Abdera();

    private static final String BASE_URL = "http://localhost:8080/namespace3/feed3/";

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
            GetMethod emptyFeedMethod = mock(GetMethod.class);
        when(httpClient.executeMethod(any(GetMethod.class))).thenReturn(HttpStatus.SC_OK);
        when(emptyFeedMethod.getResponseBodyAsString()).thenReturn("<feed xmlns='http://www.w3.org/2005/Atom'></feed>");
    }

    @Test
    public void shouldOrderCorrectlyForwardAndBackward() throws Exception {
        List<String> mockEntryIds = IntStream.range(1, 21)
                .mapToObj(i -> "urn:uuid:entry-" + i)
                .collect(Collectors.toList());

        for (String mockEntryId : mockEntryIds) {
            PostMethod postMethod = mock(PostMethod.class);
            when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
                @Override
                public boolean matches(Object o) {
                    return false;
                }

                public boolean matches(HttpMethod method) {
                    return method instanceof PostMethod &&
                            method.getRequestHeader("content-type") != null &&
                            method.getRequestHeader("content-type").getValue().contains("atom+xml");
                }
            }))).thenReturn(HttpStatus.SC_CREATED);

            when(postMethod.getResponseHeader("Location"))
                    .thenReturn(new org.apache.commons.httpclient.Header("Location", BASE_URL + mockEntryId));
        }

        String mockFeedBody = buildMockFeedBody(mockEntryIds);
        GetMethod fullFeedMethod = mock(GetMethod.class);
        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) {
                return false;
            }

            public boolean matches(HttpMethod method) {
                return method instanceof GetMethod &&
                        method.getPath().equals("/namespace3/feed3/");
            }
        }))).thenReturn(HttpStatus.SC_OK);
        when(fullFeedMethod.getResponseBodyAsString()).thenReturn(mockFeedBody);
        GetMethod backwardMethod = mock(GetMethod.class);
        String backwardFeedBody = buildMockFeedBody(mockEntryIds.subList(0, 10));
        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) {
                return false;
            }

            public boolean matches(HttpMethod method) {
                return method instanceof GetMethod &&
                        method.getQueryString() != null &&
                        method.getQueryString().contains("direction=backward");
            }
        }))).thenReturn(HttpStatus.SC_OK);
        when(backwardMethod.getResponseBodyAsString()).thenReturn(backwardFeedBody);
        GetMethod forwardMethod = mock(GetMethod.class);
        String forwardFeedBody = buildMockFeedBody(mockEntryIds.subList(9, mockEntryIds.size()));
        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) {
                return false;
            }

            public boolean matches(HttpMethod method) {
                return method instanceof GetMethod &&
                        method.getQueryString() != null &&
                        method.getQueryString().contains("direction=forward");
            }
        }))).thenReturn(HttpStatus.SC_OK);
        when(forwardMethod.getResponseBodyAsString()).thenReturn(forwardFeedBody);
        Parser parser = abdera.getParser();
        Document<Feed> doc = parser.parse(new StringReader(mockFeedBody), BASE_URL);
        Feed feed = doc.getRoot();
        assertEquals(20, feed.getEntries().size());
    }

    private String buildMockFeedBody(List<String> entryIds) {
        StringBuilder builder = new StringBuilder();
        builder.append("<feed xmlns='http://www.w3.org/2005/Atom'>");
        entryIds.forEach(id ->
                builder.append("<entry><id>").append(id).append("</id></entry>"));
        builder.append("</feed>");
        return builder.toString();
    }
}
