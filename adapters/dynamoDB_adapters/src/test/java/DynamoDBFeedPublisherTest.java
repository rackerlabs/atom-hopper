import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.dynamodb.adapter.DynamoDBFeedPublisher;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBFeedPublisherTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private DynamoDB dynamoDB;
    @Mock
    DynamoDBQueryExpression<PersistedEntry> querySpec;
    @Mock
    private DynamoDBMapper dynamoDBMapper;
    private DynamoDBFeedPublisher dynamoDBFeedPublisher = new DynamoDBFeedPublisher();
    @Mock
    private AmazonDynamoDBClient amazonDynamoDBClient;


    private final String MARKER_ID = UUID.randomUUID().toString();
    private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
    private final String FEED_NAME = "namespace/feed";
    private PersistedEntry persistedEntry;
    private List<PersistedEntry> entryList;
    private PostEntryRequest postEntryRequest;
    private PutEntryRequest putEntryRequest;
    private DeleteEntryRequest deleteEntryRequest;

    @Before
    public void setUp() throws Exception {
        dynamoDBFeedPublisher.setDynamoDBClient(amazonDynamoDBClient);
        dynamoDBFeedPublisher.setDynamoMapper(dynamoDBMapper);
        dynamoDBFeedPublisher.setDynamoDB(dynamoDB);
        persistedEntry = new PersistedEntry();
        persistedEntry.setFeed(FEED_NAME);
        persistedEntry.setEntryId(MARKER_ID);
        persistedEntry.setEntryBody(ENTRY_BODY);
        persistedEntry.setDateLastUpdated(getDateFormatInString(new Date()));
        persistedEntry.setCreationDate(getDateFormatInString(new Date()));
        entryList = new ArrayList<PersistedEntry>();
        entryList.add(persistedEntry);
        putEntryRequest = mock(PutEntryRequest.class);
        deleteEntryRequest = mock(DeleteEntryRequest.class);
        postEntryRequest = mock(PostEntryRequest.class);
        when(postEntryRequest.getEntry()).thenReturn(entry());
        when(postEntryRequest.getFeedName()).thenReturn("namespace/feed");
    }

    @Test
    public void showSaveTheObjectInDynamoDb() throws Exception {
        dynamoDBFeedPublisher.setAllowOverrideId(false);
        doNothing().when(dynamoDBMapper).save(persistedEntry);
        AdapterResponse<Entry> adapterResponse = dynamoDBFeedPublisher.postEntry(postEntryRequest);
        assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
    }

    @Test
    public void showErrorIfAlreadyExistsEntryIDInDynamoDb() throws Exception {
        final Table mockTable = mock(Table.class);
        when(dynamoDB.getTable(any(String.class))).thenReturn(mockTable);
        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);
        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(outcome);
        final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
        final Item mockItem = new Item();
        when(outcome.iterator()).thenReturn(mockIterator);
        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(mockItem);
        AdapterResponse<Entry> adapterResponse = dynamoDBFeedPublisher.postEntry(postEntryRequest);
        assertEquals("Should return HTTP 409 (Conflict)", HttpStatus.CONFLICT, adapterResponse.getResponseStatus());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldPutEntry() throws Exception {
        dynamoDBFeedPublisher.putEntry(putEntryRequest);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldDeleteEntry() throws Exception {
        dynamoDBFeedPublisher.deleteEntry(deleteEntryRequest);
    }

    public Entry entry() {
        final FOMEntry entry = new FOMEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setContent("testing");
        entry.addCategory("category");
        return entry;
    }

    public String getDateFormatInString(Date date) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
