package org.atomhopper.abdera.filter;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.FeedSourceAdapterResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class FeedPagingProcessorTest {

    public static class WhenProcessingFeedWithMoreThanOneEntry extends TestParent {

        final int TOTAL_FEED_ENTRIES = 5;

        @Test
        public void shouldAddCurrentLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();

            assertThat("Should set current link", feed.getLink(REL_CURRENT), notNullValue());
            assertThat("Should match self", feed.getLink(REL_CURRENT).getHref().toString(), equalTo(SELF_URL));
        }

        @Test
        public void shouldAddNextLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            String lastEntryId = Integer.toString(TOTAL_FEED_ENTRIES);

            assertThat("Should set next link", feed.getLink(REL_NEXT), notNullValue());
            assertThat("Should reference last entry on feed", feed.getLink(REL_NEXT).getHref().toString(), equalTo("http://localhost:8080/foo/bar?marker=" + lastEntryId));
        }

    }


    public static class WhenProcessingFeedWithOneEntry extends TestParent {
        final int TOTAL_FEED_ENTRIES = 1;

        @Test
        public void shouldAddCurrentLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();

            assertThat("Should set current link", feed.getLink(REL_CURRENT), notNullValue());
            assertThat("Should match self", feed.getLink(REL_CURRENT).getHref().toString(), equalTo(SELF_URL));
        }

        @Test
        public void shouldAddNextLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            String lastEntryId = Integer.toString(TOTAL_FEED_ENTRIES);

            assertThat("Should set next link", feed.getLink(REL_NEXT), notNullValue());
            assertThat("Should reference last entry on feed", feed.getLink(REL_NEXT).getHref().toString(), equalTo("http://localhost:8080/foo/bar?marker=" + lastEntryId));
        }

        @Test
        public void testMapToString() {

            Map<String, List<String>> test = new TreeMap<String, List<String>>();
            // Empty map returns blank string
            assertThat(FeedPagingProcessor.mapToParameters(test), equalTo(""));
            List<String> value1 = new LinkedList<String>();
            value1.add("value1");
            List<String> value2 = new LinkedList<String>();
            value2.add("value2");
            test.put("key1", value1);
            test.put("key2", value2);
            assertThat(FeedPagingProcessor.mapToParameters(test), equalTo("?key1=value1&key2=value2"));
        }

        @Test
        public void testGetParameterMap() {
            final FeedPagingProcessor target = feedPagingProcessor();

            Map<String, List<String>> map = new TreeMap<String, List<String>>();
            List<String> values = new LinkedList<String>();
            values.add("1");
            map.put("marker", values);
            Map<String, List<String>> returnedMap = target.getParameterMap(requestContext());
            assertThat("should return the expected values", returnedMap, equalTo(map));
        }

        @Test
        public void testGetMultipleParametersMap() {
            final FeedPagingProcessor target = feedPagingProcessor();

            Map<String, List<String>> map = new TreeMap<String, List<String>>();
            List<String> values = new LinkedList<String>();
            values.add("1");
            map.put("marker", values);
            List<String> foobar = new LinkedList<String>();
            foobar.add("foo");
            foobar.add("bar");

            Map<String, List<String>> returnedMap = target.getParameterMap(multiParamRequestContext());
            assertThat("should return the expected keys", returnedMap.keySet(), equalTo(map.keySet()));
            for(String key : map.keySet()) {
            }

        }


    }

    public static class WhenProcessingEmptyFeed extends TestParent {
        final int TOTAL_FEED_ENTRIES = 0;

        @Test
        public void shouldNotAddMarkers() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            assertThat("Should not set current link", feed.getLink(REL_CURRENT), nullValue());
            assertThat("Should not set next link", feed.getLink(REL_NEXT), nullValue());
        }
    }

    public static class WhenProcessingFeedWithPresetMarkers extends TestParent {

        @Test
        public void shouldNotOverrideWhenNextIsSet() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(1, true);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            assertThat("Should set current link", feed.getLink(REL_CURRENT), notNullValue());

            assertThat("Should not override next link", feed.getLink(REL_NEXT).getHref().toString(), equalTo(REL_NEXT));

        }

    }


    @Ignore
    public static class TestParent {

        static final String BASE_URI = "http://localhost:8080/";
        static final String TARGET_PATH = "/foo/bar";
        static final String TARGET_PARAMS = "?marker=1";
        static final String SELF_URL = "http://localhost:8080/foo/bar?marker=1";
        static final String REL_CURRENT = "current";
        static final String REL_NEXT = "next";


        public FeedPagingProcessor feedPagingProcessor() {
            return new FeedPagingProcessor();
        }

        public AdapterResponse<Feed> adapterResponse(int entriesOnFeed) {
            return adapterResponse(entriesOnFeed, false);
        }

        public AdapterResponse<Feed> adapterResponse(int entriesOnFeed, boolean hasNextMarker) {
            final Feed feed = Abdera.getInstance().newFeed();

            for (int i = 1; i <= entriesOnFeed; i++) {
                Entry entry = Abdera.getInstance().newEntry();
                entry.setId(Integer.toString(i));
                feed.addEntry(entry);
            }

            if (hasNextMarker) {
                feed.addLink("next", REL_NEXT);
            }

            return new FeedSourceAdapterResponse<Feed>(feed, HttpStatus.OK, "");
        }

        public RequestContext requestContext() {
            RequestContext target = mock(RequestContext.class);

            when(target.getResolvedUri()).thenReturn(new IRI(SELF_URL));
            when(target.getBaseUri()).thenReturn(new IRI(BASE_URI));
            when(target.getTargetPath()).thenReturn(TARGET_PATH + TARGET_PARAMS);
            when(target.getParameterNames()).thenReturn(new String[]{"marker"});
            //when(target.getParameter("marker")).thenReturn("1");
            List<String> mockedValues = new LinkedList<String>();
            mockedValues.add("1");
            when(target.getParameters("marker")).thenReturn(mockedValues);


            return target;
        }

        public RequestContext multiParamRequestContext() {
            RequestContext target = mock(RequestContext.class);
            when(target.getResolvedUri()).thenReturn(new IRI(SELF_URL));
            when(target.getBaseUri()).thenReturn(new IRI(BASE_URI));
            when(target.getTargetPath()).thenReturn(TARGET_PATH + TARGET_PARAMS);
            when(target.getParameterNames()).thenReturn(new String[]{"marker"});
            //when(target.getParameter("marker")).thenReturn("1");
            List<String> mockedValues = new LinkedList<String>();
            mockedValues.add("1");
            when(target.getParameters("marker")).thenReturn(mockedValues);
            List<String> foobar = new LinkedList<String>();
            foobar.add("foo");
            foobar.add("bar");
            when(target.getParameters("foobar")).thenReturn(foobar);
            return target;
        }
    }
}
