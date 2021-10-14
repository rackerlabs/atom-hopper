package org.atomhopper.dynamodb.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for the methods declared in the dynamoDB feed Source.
 */
public class JsonUtil {
    static Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * This method is used to return the date from date interval of 2 seconds
     * like now() - interval '2 seconds
     *
     * @param seconds: 2 seconds
     * @return date format
     */
    public static String getCurrentDateWithMinusSecond(int seconds) {
        LOG.info("getCurrentDateWithMinusSecond");
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.SECOND, (calendar.get(Calendar.SECOND) - seconds));
        LOG.info("RETURN RESULT VALUE:" + formatter.format(calendar.getTime()));
        return formatter.format(calendar.getTime());
    }

    /**
     * This method is used to parse list of string into PersistentEntry using object mapper
     *
     * @param feedPage: List of json object
     * @return List of PersistentEntry
     */

    public static List<PersistedEntry> getPersistenceEntity(List<String> feedPage) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<PersistedEntry> jsonToPersistentList = new ArrayList<>();
        //2. Convert JSON to List of Person objects
        //2. Convert JSON to List of PersistedEntry objectschanges
        //Define Custom Type reference for List<PersistedEntry> type
        TypeReference<PersistedEntry> mapType = new TypeReference<PersistedEntry>() {
        };
        for (String s : feedPage) {
            try {
                jsonToPersistentList.add(objectMapper.readValue(s, mapType));
                LOG.info("PERSISTENT ENTRY DATA:" + jsonToPersistentList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonToPersistentList;
    }
}
