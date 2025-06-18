package org.atomhopper.abdera.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


public class JsonToPojo{

    public static final ObjectMapper mapper = new ObjectMapper();
//taking the inputStream and generating instance of EntryJSON
    public static EntryJsonPOJO fromInputStream(InputStream in) throws IOException{
        return mapper.readValue(in, EntryJsonPOJO.class);
    }
//converting reader to POJO
    public static EntryJsonPOJO fromReader(Reader reader) throws IOException{
        return mapper.readValue(reader, EntryJsonPOJO.class);
    }

}
