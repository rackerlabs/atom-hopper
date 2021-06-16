import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.dynamodb.adapter.DynamoFeedPublisher;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.atomhopper.response.AdapterResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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
    private DynamoDBMapper dynamoDBMapper;

    private DynamoFeedPublisher dynamoFeedPublisher = new DynamoFeedPublisher();
    //        @Mock
//        private DynamoDBMapperConfig dynamoDBMapperConfig;
//        @Mock
//        private AmazonDynamoDB dynamoDB;
    @Mock
    private AmazonDynamoDBClient amazonDynamoDBClient;
//        @Mock
//        private ApplicationContext applicationContext;
//
//        private DynamoDBTemplate dynamoDBTemplate;

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
        dynamoFeedPublisher.setDynamoDBClient(amazonDynamoDBClient);
        dynamoFeedPublisher.setDynamoMapper(dynamoDBMapper);
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

//        persistedEntry = new PersistedEntry();
//        persistedEntry.setFeed(FEED_NAME);
//        persistedEntry.setEntryId(MARKER_ID);
//        persistedEntry.setEntryBody(ENTRY_BODY);
//
//        entryList = new ArrayList<PersistedEntry>();
//        entryList.add(persistedEntry);

    }

    @Test
    public void showSaveTheObjectInDynamoDb() throws Exception {
        doNothing().when(dynamoDBMapper).save(persistedEntry);
        when(dynamoDBMapper.load(PersistedEntry.class, persistedEntry.getEntryId()))
                .thenReturn(persistedEntry);
        AdapterResponse<Entry> adapterResponse = dynamoFeedPublisher.postEntry(postEntryRequest);
        assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
    }

    @Test
    public void showErrorIfAlreadyExistsEntryIDInDynamoDb() throws Exception {
        dynamoFeedPublisher.setAllowOverrideId(true);
       when(dynamoDBMapper.load(PersistedEntry.class,postEntryRequest.getEntry(),postEntryRequest.getFeedName())).thenReturn(persistedEntry);
        AdapterResponse<Entry> adapterResponse = dynamoFeedPublisher.postEntry(postEntryRequest);
        assertEquals("Should return HTTP 409 (Conflict)", HttpStatus.CONFLICT, adapterResponse.getResponseStatus());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldPutEntry() throws Exception {
        dynamoFeedPublisher.putEntry(putEntryRequest);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldDeleteEntry() throws Exception {
        dynamoFeedPublisher.deleteEntry(deleteEntryRequest);
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
