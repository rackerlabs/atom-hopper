package org.atomhopper.util.jsonparsingwork;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.stax.FOMParser;
import org.apache.abdera.writer.Writer;
import org.apache.abdera.writer.WriterFactory;
import org.atomhopper.abdera.parser.UnifiedParser;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class UnifiedParserTest {

    private UnifiedParser unifiedParser;
    private ParserOptions options;
    private String sampleJson;
    private String sampleXml;

    @Before
    public void setUp() {
        Parser abderaParser = new FOMParser();
        unifiedParser = new UnifiedParser(abderaParser);
        options = new FOMParser().getDefaultParserOptions();
        sampleJson = "{\n" +
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

        sampleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<?atom feed=\"autoscale/events\"?>\n" +
                "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\"\n" +
                "            xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "            xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <atom:title>autoscale</atom:title>\n" +
                "    <atom:content type=\"application/xml\">\n" +
                "        <event xmlns=\"http://docs.rackspace.com/core/event\"\n" +
                "               xmlns:sample=\"http://docs.rackspace.com/event/autoscale\"\n" +
                "               id=\"e53d007a-fc23-11e1-975c-cfa6b29bb814\"\n" +
                "               version=\"2\"\n" +
                "               eventTime=\"2013-03-15T11:51:11Z\"\n" +
                "               type=\"INFO\"\n" +
                "               region=\"GLOBAL\"\n" +
                "               dataCenter=\"GLOBAL\">\n" +
                "            <sample:product serviceCode=\"Autoscale\"\n" +
                "                            version=\"1\"\n" +
                "                            scalingGroupId=\"6e8bc430-9c3a-11d9-9669-0800200c9a66\"\n" +
                "                            desiredCapacity=\"5\"\n" +
                "                            currentCapacity=\"3\"\n" +
                "                            message=\"Launching 2 servers\"/>\n" +
                "        </event>\n" +
                "    </atom:content>\n" +
                "</atom:entry>";
    }

    // Test valid JSON content types
    @Test
    public void testValidJsonParsing() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        Document<?> doc = unifiedParser.parseWithContentType(stream, "application/json", options);
        assertNotNull(doc);
        assertTrue(doc.getRoot() instanceof Entry);
        assertEquals("entry", doc.getRoot().getQName().getLocalPart());
    }

    @Test
    public void testValidJsonWithAtomJson() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        Document<?> doc = unifiedParser.parseWithContentType(stream, "application/atom+json", options);
        assertNotNull(doc);
        assertTrue(doc.getRoot() instanceof Entry);
    }

    @Test
    public void testValidJsonWithTextJson() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        Document<?> doc = unifiedParser.parseWithContentType(stream, "text/json", options);
        assertNotNull(doc);
        assertTrue(doc.getRoot() instanceof Entry);
    }

    // Test valid XML parsing
    @Test
    public void testValidXmlParsing() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        Document<Element> doc = unifiedParser.parseWithContentType(stream, "application/atom+xml", options);
        assertNotNull(doc);
        assertEquals("entry", doc.getRoot().getQName().getLocalPart());
    }

    @Test
    public void testValidXmlWithApplicationXml() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        Document<Element> doc = unifiedParser.parseWithContentType(stream, "application/xml", options);
        assertNotNull(doc);
        assertEquals("entry", doc.getRoot().getQName().getLocalPart());
    }

    @Test
    public void testValidXmlWithTextXml() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        Document<Element> doc = unifiedParser.parseWithContentType(stream, "text/xml", options);
        assertNotNull(doc);
        assertEquals("entry", doc.getRoot().getQName().getLocalPart());
    }

    // Test content type variations
    @Test
    public void testContentTypeWithCharset() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        Document<?> doc = unifiedParser.parseWithContentType(
                stream,
                "application/json; charset=UTF-8",
                options
        );
        assertNotNull(doc);
    }

    @Test
    public void testContentTypeWithMultipleParameters() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        Document<Element> doc = unifiedParser.parseWithContentType(
                stream,
                "application/atom+xml; charset=UTF-8; boundary=something",
                options
        );
        assertNotNull(doc);
    }

    // Test Reader variants
    @Test
    public void testValidJsonReader() throws Exception {
        try (Reader reader = new StringReader(sampleJson)) {
            Document<?> doc = unifiedParser.parseWithContentType(reader, "application/json", options);
            assertNotNull(doc);
            assertTrue(doc.getRoot() instanceof Entry);
        }
    }

    @Test
    public void testValidXmlReader() throws Exception {
        try (Reader reader = new StringReader(sampleXml)) {
            Document<Element> doc = unifiedParser.parseWithContentType(reader, "application/atom+xml", options);
            assertNotNull(doc);
            assertEquals("entry", doc.getRoot().getQName().getLocalPart());
        }
    }

    // Test invalid cases
    @Test(expected = IllegalArgumentException.class)
    public void testNullInputStream() throws Exception {
        unifiedParser.parseWithContentType((InputStream)null, "application/json", options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullReader() throws Exception {
        unifiedParser.parseWithContentType((Reader)null, "application/json", options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContentType() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        unifiedParser.parseWithContentType(stream, "", options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullContentType() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        unifiedParser.parseWithContentType(stream, null, options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsupportedContentType() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        unifiedParser.parseWithContentType(stream, "text/plain", options);
    }

    @Test(expected = Exception.class)
    public void testMismatchedJsonContentTypeWithXmlBody() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        unifiedParser.parseWithContentType(stream, "application/json", options);
    }

    @Test(expected = Exception.class)
    public void testMismatchedXmlContentTypeWithJsonBody() throws Exception {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        unifiedParser.parseWithContentType(stream, "application/xml", options);
    }

    // Test empty input
    @Test(expected = Exception.class)
    public void testEmptyJsonInput() throws Exception {
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        unifiedParser.parseWithContentType(empty, "application/json", options);
    }

    @Test(expected = Exception.class)
    public void testEmptyXmlInput() throws Exception {
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        unifiedParser.parseWithContentType(empty, "application/xml", options);
    }
}