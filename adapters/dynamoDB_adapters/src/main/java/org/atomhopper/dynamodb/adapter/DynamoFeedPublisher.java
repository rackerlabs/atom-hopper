package org.atomhopper.dynamodb.adapter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.*;
import com.yammer.metrics.Metrics;
import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yammer.metrics.core.Counter;

import java.io.IOException;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;

public class DynamoFeedPublisher implements FeedPublisher {

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper mapper;

    /*private DynamoFeedPublisher(AmazonDynamoDBClient dynamoDBClient){
        this.dynamoDBClient=dynamoDBClient;
        this.mapper=new DynamoDBMapper(dynamoDBClient);
    }*/

    public void setDynamoDBClient(AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
        setDynamoMapper(new DynamoDBMapper(dynamoDBClient));
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
        DynamoDBFeedSource dynamoDBFeedSource = new DynamoDBFeedSource(dynamoDBMapper);
//        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
//        String dateLastUpdated = dateFormatter.format(new Date());
//        PersistedEntry persistedEntry = new PersistedEntry();
//        persistedEntry.setEntryId("101");
//        List<String> cat = new ArrayList<String>();
//        cat.add("cat1");
//        cat.add("cat2");
//        persistedEntry.setCategories(cat);
//        persistedEntry.setFeed("namespace/feed");
//        persistedEntry.setDateLastUpdated(dateLastUpdated);
//        dynamoDBMapper.save(persistedEntry);
//
//        PersistedEntry persistedEntry1 = new PersistedEntry();
//        persistedEntry1.setEntryId("101");
//        List<String> cat1 = new ArrayList<String>();
//        cat1.add("cat1");
//        persistedEntry1.setCategories(cat1);
//        persistedEntry1.setFeed("namespace/feed2");
//        dateLastUpdated = dateFormatter.format(new Date());
//        persistedEntry1.setDateLastUpdated(dateLastUpdated);
//        dynamoDBMapper.save(persistedEntry1);
//
//        PersistedEntry persistedEntry2 = new PersistedEntry();
//        persistedEntry2.setEntryId("103");
//        List<String> cat2 = new ArrayList<String>();
//        cat2.add("cat3");
//        persistedEntry2.setCategories(cat2);
//        persistedEntry2.setFeed("namespace/feed3");
//        dateLastUpdated = dateFormatter.format(new Date());
//        persistedEntry2.setDateLastUpdated(dateLastUpdated);
//        dynamoDBMapper.save(persistedEntry2);
       // List<PersistedEntry> list = dynamoDBFeedSource.getFeedBackward("",new Date(),103,"(NOT(cat=cat1))",20);

    }

    public void setDynamoMapper(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    private static final Logger LOG = LoggerFactory.getLogger(DynamoFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;

    private Map<String, Counter> counterMap = Collections.synchronizedMap(new HashMap<String, Counter>());

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    public void setAllowOverrideId(boolean allowOverrideId) {
        this.allowOverrideId = allowOverrideId;
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry abderaParsedEntry = postEntryRequest.getEntry();
        final PersistedEntry persistedEntry = new PersistedEntry();
        boolean entryIdSent = abderaParsedEntry.getId() != null;

        // Generate an ID for this entry
        if (allowOverrideId && entryIdSent && StringUtils.isNotBlank(abderaParsedEntry.getId().toString().trim())) {
            String entryId = abderaParsedEntry.getId().toString();
            // Check to see if entry with this id already exists
            PersistedEntry exists = getEntry(entryId, postEntryRequest.getFeedName());
            if (exists != null) {
                String errMsg = String.format("Unable to persist entry. Reason: entryId (%s) not unique.", entryId);
                return ResponseBuilder.conflict(errMsg);
            }
            persistedEntry.setEntryId(abderaParsedEntry.getId().toString());
        } else {
            persistedEntry.setEntryId(UUID_URI_SCHEME + UUID.randomUUID().toString());
            abderaParsedEntry.setId(persistedEntry.getEntryId());
        }
        if (allowOverrideDate) {
            Date updated = abderaParsedEntry.getUpdated();

            if (updated != null) {
                persistedEntry.setDateLastUpdated(getDateFormatInString(updated));
                persistedEntry.setCreationDate(getDateFormatInString(updated));
            }
        }

        // Set the categories
        persistedEntry.setCategories(processCategories(abderaParsedEntry.getCategories()));

        if (abderaParsedEntry.getSelfLink() == null) {
            abderaParsedEntry.addLink(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                    + "entries/" + persistedEntry.getEntryId()).setRel(LINKREL_SELF);
        }

        persistedEntry.setFeed(postEntryRequest.getFeedName());
        persistedEntry.setEntryBody(entryToString(abderaParsedEntry));
        abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());
        abderaParsedEntry.setPublished(persistedEntry.getCreationDate());
        mapper.save(persistedEntry);//dynamoDB save object

        incrementCounterForFeed(postEntryRequest.getFeedName());
        return ResponseBuilder.created(abderaParsedEntry);
    }

    private List processCategories(List<org.apache.abdera.model.Category> abderaCategories) {
        final List<String> categoriesList = new ArrayList<String>();

        for (org.apache.abdera.model.Category abderaCat : abderaCategories) {
            categoriesList.add(abderaCat.getTerm().toLowerCase());
        }

        return categoriesList;
    }

    private String entryToString(Entry entry) {
        final StringWriter writer = new StringWriter();

        try {
            entry.writeTo(writer);
        } catch (IOException ioe) {
            LOG.error("Unable to write entry to string. Unable to persist entry. Reason: " + ioe.getMessage(), ioe);

            throw new PublicationException(ioe.getMessage(), ioe);
        }

        return writer.toString();
    }

    @Override
    @NotImplemented
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @NotImplemented
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * To get the entry from dynamodb based upon two params?
     *
     * @param entryId
     * @param feedName
     * @return
     */
    private PersistedEntry getEntry(String entryId, final String feedName) {
        return mapper.load(PersistedEntry.class, entryId, feedName);//get the mapper object from dynamodb
    }

    private void incrementCounterForFeed(String feedName) {

        if (!counterMap.containsKey(feedName)) {
            synchronized (counterMap) {
                if (!counterMap.containsKey(feedName)) {
                    Counter counter = Metrics.newCounter(DynamoFeedPublisher
                            .class, "entries-created-for-" + feedName);
                    counterMap.put(feedName, counter);
                }
            }
        }

        counterMap.get(feedName).inc();
    }

    /**
     * Sets the date in String format as we save the date in string in dynamodb.
     *
     * @param date Date Object to be formatted in string.
     * @return
     */
    private String getDateFormatInString(Date date) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
