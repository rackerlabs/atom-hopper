package org.atomhopper.dynamodb.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atomhopper.dynamodb.model.PersistedEntry;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for the methods declared in the dynamoDB feed Source.
 */
public class JsonUtil {

    /**
     * This method is used to return the date from date interval of 2 seconds
     * like now() - interval '2 seconds
     *
     * @param seconds: 2 seconds
     * @return date format
     */
    public static String getCurrentDateWithMinusSecond(int seconds) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.SECOND, (calendar.get(Calendar.SECOND) - seconds));
        return formatter.format(calendar.getTime());
    }

    /**
     * This method is used to parse list of string into PersistentEntry using object mapper
     *
     * @param feedPage: List of json object
     * @return List of PersistentEntry
     */

    public static List<PersistedEntry> getPersistenceEntity(List feedPage) {
        ObjectMapper objectMapper = new ObjectMapper();
        String arrayToJson = null;
        try {
            arrayToJson = objectMapper.writeValueAsString(feedPage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //2. Convert JSON to List of Person objects
        //Define Custom Type reference for List<PersistedEntry> type
        TypeReference<List<PersistedEntry>> mapType = new TypeReference<List<PersistedEntry>>() {
        };
        List<PersistedEntry> jsonToPersonList = null;
        try {
            jsonToPersonList = objectMapper.readValue(arrayToJson, mapType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonToPersonList;
    }


}
