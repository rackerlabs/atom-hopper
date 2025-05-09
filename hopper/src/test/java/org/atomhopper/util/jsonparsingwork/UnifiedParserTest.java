package org.atomhopper.util.jsonparsingwork;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.parser.stax.FOMParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/application-context.xml" })
public class UnifiedParserTest {

    @Autowired
    private UnifiedParser parser;

    private final ParserOptions options = new FOMParser().getDefaultParserOptions();

    @Test
    public void testValidJsonInputStream() {
        String json = "{\n" +
                "    \"entry\": {\n" +
                "        \"@type\": \"http://www.w3.org/2005/Atom\",\n" +
                "        \"title\": \"autoscale\",\n" +
                "        \"content\": {\n" +
                "            \"event\": {\n" +
                "                \"@type\": \"http://docs.rackspace.com/core/event\",\n" +
                "                \"id\": \"e53d007a-fc23-11f1-975c-cfa6b29bb814\",\n" +
                "                \"version\": \"2\",\n" +
                "                \"eventTime\": \"2013-03-15T11:51:11Z\",\n" +
                "                \"type\": \"INFO\",\n" +
                "                \"dataCenter\": \"DFW1\",\n" +
                "                \"region\": \"DFW\",\n" +
                "                \"product\": {\n" +
                "                    \"@type\": \"http://docs.rackspace.com/event/autoscale\",\n" +
                "                    \"serviceCode\": \"Autoscale\",\n" +
                "                    \"version\": \"1\",\n" +
                "                    \"scalingGroupId\": \"6e8bc430-9c3a-11d9-9669-0800200c9a66\",\n" +
                "                    \"desiredCapacity\": 5,\n" +
                "                    \"currentCapacity\": 3,\n" +
                "                    \"message\": \"Launching 2 servers\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        InputStream stream = new ByteArrayInputStream(json.getBytes());

        Document<?> doc = parser.parse(stream, "application/json", options);

        assertNotNull("Parsed document should not be null", doc);
        assertEquals("autoscale", ((Entry) doc.getRoot()).getTitle());
    }
}
