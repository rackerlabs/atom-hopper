import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dynamodb.adapter.DynamoDBFeedSource;
import org.atomhopper.dynamodb.adapter.DynamoDBFeedPublisher;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.atomhopper.dynamodb.query.JsonUtil;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.net.URL;
import java.util.*;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBFeedSourceTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private DynamoDBMapper dynamoDBMapper;
    @Mock
    private DynamoDB dynamoDB;
    @Mock
    private AmazonDynamoDBClient amazonDynamoDBClient;
    @Mock
    private PaginatedQueryList<PersistedEntry> paginatedQueryList;
    private DynamoDBFeedPublisher dynamoFeedDBPublisher = new DynamoDBFeedPublisher();
    private GetFeedRequest getFeedRequest;
    private DynamoDBFeedSource dynamoDBFeedSource;
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
    private final String NOT_CAT = "(NOT(cat=CAT1))";
    private final String MOCK_LAST_MARKER = "last";
    private final String NEXT_ARCHIVE = "next-archive";
    private final String ARCHIVE_LINK = "http://archive.com/namespace/feed/archive";
    private final String CURRENT = "current";


    @Before
    public void setUp() throws Exception {
        dynamoDBFeedSource = new DynamoDBFeedSource(amazonDynamoDBClient);
        dynamoFeedDBPublisher.setDynamoDBClient(amazonDynamoDBClient);
        dynamoFeedDBPublisher.setDynamoMapper(dynamoDBMapper);
        dynamoDBFeedSource.setDynamoDB(dynamoDB);
        persistedEntry = new PersistedEntry();
        persistedEntry.setFeed(FEED_NAME);
        persistedEntry.setEntryId(MARKER_ID);
        persistedEntry.setEntryBody(ENTRY_BODY);

        emptyList = new ArrayList<PersistedEntry>();

        entryList = new ArrayList<PersistedEntry>();
        entryList.add(persistedEntry);

        // Mocks
        abdera = mock(Abdera.class);
        getFeedRequest = mock(GetFeedRequest.class);
        getEntryRequest = mock(GetEntryRequest.class);

        dynamoDBFeedSource.setArchiveUrl(new URL(ARCHIVE_LINK));

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
    public void shouldNotGetFeedWithMarkerDirectionForward() throws Exception {
        Abdera localAbdera = new Abdera();
        when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
        when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
        when(getFeedRequest.getDirection()).thenReturn("FORWARD");
        final Table mockTable = mock(Table.class);
        when(dynamoDB.getTable(any(String.class))).thenReturn(mockTable);
        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);
        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(outcome);
        final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
        final Item mockItem = new Item();
        when(outcome.iterator()).thenReturn(mockIterator);
        when(mockIterator.hasNext()).thenReturn(false);
        when(mockIterator.next()).thenReturn(mockItem);
        assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND,
                dynamoDBFeedSource.getFeed(getFeedRequest).getResponseStatus());
    }

    @Test
    public void shouldNotGetFeedWithMarkerDirectionBackward() throws Exception {
        Abdera localAbdera = new Abdera();
        when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
        when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
        when(getFeedRequest.getDirection()).thenReturn("BACKWARD");
        final Table mockTable = mock(Table.class);
        when(dynamoDB.getTable(any(String.class))).thenReturn(mockTable);
        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);
        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(outcome);
        final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
        final Item mockItem = new Item();
        when(outcome.iterator()).thenReturn(mockIterator);
        when(mockIterator.hasNext()).thenReturn(false);
        when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
        when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
        when(getFeedRequest.getDirection()).thenReturn("BACKWARD");
        assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND,
                dynamoDBFeedSource.getFeed(getFeedRequest).getResponseStatus());
    }


    @Test
    public void shouldGetFeedHead() throws Exception {
        Abdera localAbdera = new Abdera();
        List<String> newList = new ArrayList<>();
        when(getFeedRequest.getDirection()).thenReturn("forward");
        when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
        when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
        final Table mockTable = mock(Table.class);
        when(dynamoDB.getTable(any(String.class))).thenReturn(mockTable);
        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);
        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(outcome);
        final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
        final Item mockItem = new Item();
        when(outcome.iterator()).thenReturn(mockIterator);
        when(mockIterator.hasNext()).thenReturn(false);
        when(mockIterator.next()).thenReturn(mockItem);
        newList.add("fe9eedc8-d10c-47a6-9209-f755ea8c35c3");
        newList.add("<entry xmlns='http://www.w3.org/2005/Atom'></entry>");
        DynamoDBFeedSource mainModel = Mockito.mock(DynamoDBFeedSource.class);
        Mockito.when(mainModel.getQueryBuilderMethod(dynamoDB, "feed = :feed and dateLastUpdated < :dateLastUpdated", 25, new ValueMap(), true)).thenReturn(newList);
        assertEquals("Should get a 200 response", HttpStatus.OK,
                dynamoDBFeedSource.getFeed(getFeedRequest).getResponseStatus());
    }


    @Test(expected = RuntimeException.class)
    public void shouldReturnFeedWithCorrectTimeStampForForwardDirection() throws Exception {
        final Table mockTable = mock(Table.class);
        when(dynamoDB.getTable(any(String.class))).thenReturn(mockTable);
        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);
        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(outcome);
        final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
        final Item mockItem = new Item();
        when(outcome.iterator()).thenReturn(mockIterator);
        when(mockIterator.hasNext()).thenReturn(false);
        when(mockIterator.next()).thenReturn(mockItem);
        when(getFeedRequest.getDirection()).thenReturn(BACKWARD);
        dynamoDBFeedSource.getFeedPageByTimestamp(getFeedRequest, "2014-03-10T00:00:00.000Z", 25);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void shouldSetParameters() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("test1", "test2");
        dynamoDBFeedSource.setParameters(map);


    }

}

