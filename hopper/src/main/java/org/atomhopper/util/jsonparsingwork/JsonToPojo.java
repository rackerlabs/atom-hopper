package org.atomhopper.util.jsonparsingwork;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;


public class JsonToPojo{

    public static final ObjectMapper mapper = new ObjectMapper();
//taking the inputStream and generating instance of EntryJSON
    public static EntryJsonPOJO.Root fromInputStream(InputStream in) throws IOException{
        return mapper.readValue(in, EntryJsonPOJO.Root.class);
    }
//converting reader to POJO
    public static EntryJsonPOJO.Root fromReader(Reader reader) throws IOException{
        return mapper.readValue(reader, EntryJsonPOJO.Root.class);
    }

}
