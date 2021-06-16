import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.apache.abdera.Abdera;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dynamodb.adapter.DynamoDBFeedSource;
import org.atomhopper.dynamodb.adapter.DynamoFeedPublisher;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class DynamoDBFeedSourceTest {

    public static class WhenSourcingFeeds {

        @Rule
        public ExpectedException expectedException = ExpectedException.none();
        @Mock
        private DynamoDBMapper dynamoDBMapper;
        @Mock
        private AmazonDynamoDBClient amazonDynamoDBClient;
        @Mock
        private PaginatedQueryList<PersistedEntry> paginatedQueryList;
        private DynamoFeedPublisher dynamoFeedPublisher = new DynamoFeedPublisher();
        private GetFeedRequest getFeedRequest;
        private DynamoDBFeedSource dynamoDBFeedSource = new DynamoDBFeedSource(dynamoDBMapper);
        private GetEntryRequest getEntryRequest;
        private PersistedEntry persistedEntry;
        private List<PersistedEntry> entryList;
        private List<PersistedEntry> emptyList;
        private Abdera abdera;
        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private final String FORWARD = "forward";
        private final String BACKWARD = "backward";
        private final String SINGLE_CAT = "+Cat1";
        private final String MULTI_CAT = "+Cat1+Cat2";
        private final String AND_CAT = "(AND(cat=cat1)(cat=cat2))";
        private final String OR_CAT = "(OR(cat=cat1)(cat=cat2))";
        private final String NOT_CAT = "(AND(cat=CAT1)(OR(cat=CAT2)(cat=CAT3))(NOT(cat=CAT4)))";
        private final String MOCK_LAST_MARKER = "last";
        private final String NEXT_ARCHIVE = "next-archive";
        private final String ARCHIVE_LINK = "http://archive.com/namespace/feed/archive";
        private final String CURRENT = "current";


        @Before
        public void setUp() throws Exception {
            dynamoFeedPublisher.setDynamoDBClient(amazonDynamoDBClient);
            dynamoFeedPublisher.setDynamoMapper(dynamoDBMapper);
            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

            emptyList = new ArrayList<PersistedEntry>();

            entryList.add(persistedEntry);

            // Mocks
            abdera = mock(Abdera.class);
            getFeedRequest = mock(GetFeedRequest.class);
            getEntryRequest = mock(GetEntryRequest.class);

            // Mock GetEntryRequest
            when(getEntryRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getEntryRequest.getEntryId()).thenReturn(MARKER_ID);

            //Mock GetFeedRequest
            when(getFeedRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getFeedRequest.getPageSize()).thenReturn("25");
            when(getFeedRequest.getAbdera()).thenReturn(abdera);


        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionForPrefixColumnMap() throws Exception {

            dynamoDBFeedSource.setDelimiter(":");
            dynamoDBFeedSource.afterPropertiesSet();
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionForDelimiter() throws Exception {

            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "testA");

            dynamoDBFeedSource.setPrefixColumnMap(map);
            dynamoDBFeedSource.afterPropertiesSet();
        }


        @Test
        public void shouldGetFeedWithNotCategoriesWithMarkerBackward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(BACKWARD);
            when(getFeedRequest.getSearchQuery()).thenReturn(NOT_CAT);
            Abdera localAbdera = new Abdera();
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            String filters = "( not  (contains(categories,:cat1)) )";
            Map<String, String> valueMap = new HashMap<String, String>();
            valueMap.put(":dateLastUpdated ", "{S: 2021-06-16T11:24:13.261Z,}");
            valueMap.put(":id ", "{S: 101,}");
            valueMap.put(":cat1 ", "{S: cat1,}");
            DynamoDBQueryExpression<PersistedEntry> querySpec = new DynamoDBQueryExpression()
                    .withKeyConditionExpression("entryId = :id and dateLastUpdated <= :dateLastUpdated")
                    .withScanIndexForward(false)
                    .withLimit(25)
                    .withFilterExpression(filters)
                    .withExpressionAttributeValues(valueMap);
            //when(dynamoDBMapper.query(PersistedEntry.class, Matchers.<DynamoDBQueryExpression<PersistedEntry>>any())).thenReturn((PaginatedQueryList<PersistedEntry>) entryList);
            when(dynamoDBMapper.query(eq(PersistedEntry.class), any(DynamoDBQueryExpression.class))).thenReturn(paginatedQueryList);
            assertEquals(entryList.size(),
                    dynamoDBFeedSource.getFeedBackward("namespace/feed", new Date(), Long.parseLong(persistedEntry.getEntryId()), NOT_CAT, 25).size());
        }


        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            dynamoDBFeedSource.setParameters(map);
        }

    }

}
