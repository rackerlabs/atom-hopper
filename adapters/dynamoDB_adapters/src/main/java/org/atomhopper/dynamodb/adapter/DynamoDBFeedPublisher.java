package org.atomhopper.dynamodb.adapter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
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
import org.atomhopper.dynamodb.constant.DynamoDBConstant;
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

/**
 * @Author: shub6691
 * This class is used to publish the records in dynamodb using EntryId,feedName .
 * Index has been created on the feedName for fast search and getting the entry from the DynamoDb.
 */
public class DynamoDBFeedPublisher implements FeedPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoDBFeedPublisher.class);
    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper mapper;
    private DynamoDB dynamoDB;


    public void setDynamoDBClient(AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
        setDynamoMapper(new DynamoDBMapper(dynamoDBClient));
        this.dynamoDB = new DynamoDB(dynamoDBClient);
        setDynamoDB(dynamoDB);
    }

    public void setDynamoMapper(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    public void setDynamoDB(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    private boolean allowOverrideId = true;
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

    /**
     * This method is used to post a new feed into DynamoDB as per the request by using the APACHE ABDERA LIBRARY
     * @param postEntryRequest: This object has all the data for the feed to be published into DynamoDB which is
     *                        parsed using the Abdera library.
     * @return Return the response of the feed in format of atom format
     */
    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry abderaParsedEntry = postEntryRequest.getEntry();
        final PersistedEntry persistedEntry = new PersistedEntry();
        boolean entryIdSent = abderaParsedEntry.getId() != null;

        // Generate an ID for this entry
        if (allowOverrideId && entryIdSent && StringUtils.isNotBlank(abderaParsedEntry.getId().toString().trim())) {
            String entryId = abderaParsedEntry.getId().toString();
            // Check to see if entry with this id already exists
            //Returns List of Object found with the entryId and feedName;
            List<String> exists = getEntry(entryId, postEntryRequest.getFeedName());
            if (!exists.isEmpty()) {
                String errMsg = String.format("Unable to persist entry. Reason: entryId (%s) not unique.", entryId);
                return ResponseBuilder.conflict(errMsg);
            }
            persistedEntry.setEntryId(abderaParsedEntry.getId().toString());
        } else {
            persistedEntry.setEntryId(DynamoDBConstant.UUID_URI_SCHEME + UUID.randomUUID().toString());
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
                    + "entries/" + persistedEntry.getEntryId()).setRel(DynamoDBConstant.LINK_REL_SELF);
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
     * @param entryId:  It is the marker id for entry for every events
     * @param feedName: feed name is used to search the records in dynamodb
     * @return : list of entry found if that exits in dynamodb with the entryId and feedName.
     */
    private List<String> getEntry(String entryId, final String feedName) {
        List<String> persistedEntriesObject = new ArrayList<String>();
        Table table = dynamoDB.getTable(DynamoDBConstant.ENTRIES);
        Index index = table.getIndex(DynamoDBConstant.ENTRY_ID_FEED_INDEX);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("entryId = :entryId and feed = :feed")
                .withValueMap(new ValueMap()
                        .withString(":entryId", entryId)
                        .withString(":feed", feedName));
        ItemCollection<QueryOutcome> persistedEntryItems = index.query(spec);
        Iterator<Item> itemsIterator = persistedEntryItems.iterator();
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();
            persistedEntriesObject.add(item.toJSONPretty());
        }
        return persistedEntriesObject;
    }

    private void incrementCounterForFeed(String feedName) {

        if (!counterMap.containsKey(feedName)) {
            synchronized (counterMap) {
                if (!counterMap.containsKey(feedName)) {
                    Counter counter = Metrics.newCounter(DynamoDBFeedPublisher
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
