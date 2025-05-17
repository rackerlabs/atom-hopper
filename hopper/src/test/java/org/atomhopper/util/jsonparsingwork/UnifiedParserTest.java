package org.atomhopper.util.jsonparsingwork;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.parser.stax.FOMParser;
import org.apache.abdera.writer.Writer;
import org.apache.abdera.writer.WriterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

import static org.junit.Assert.*;


public class UnifiedParserTest {

    private UnifiedParser unifiedParser;
    private ParserOptions options;
    private String sampleJson;
    private String sampleXml;

    @Before
    public void setUp() {
        unifiedParser = new UnifiedParser();
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

    @Test
    public void testValidJsonInputStream() throws IOException {

        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());

        Document<?> doc = unifiedParser.parse(stream, "application/json", options);

        assertNotNull("Parsed document should not be null", doc);
        assertTrue(doc.getRoot() instanceof Entry);
        assertEquals("entry", doc.getRoot().getQName().getLocalPart());

        //trying to print
        WriterFactory writerFactory = new Abdera().getWriterFactory();
        Writer writer = writerFactory.getWriter("prettyxml");
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(doc, stringWriter);

        System.out.println("Parsed JSON Document as Abdera entry:\n" + stringWriter);

    }

    @Test
    public void testValidXmlInputStream() throws IOException {

        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        Document<Element> result = unifiedParser.parse(stream, "application/atom+xml", options);
        //shouldnt be null
        assertNotNull("Parsed document should not be null", result);
        assertEquals("entry", result.getRoot().getQName().getLocalPart());

        WriterFactory writerFactory = new Abdera().getWriterFactory();
        Writer writer = writerFactory.getWriter("prettyxml");
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(result, stringWriter);
        System.out.println("Parsed XML Document as Abdera Entry:\n" + stringWriter.toString());
    }

    @Test
    public void testValidJsonReader() throws IOException {

        Reader jsonReader = new StringReader(sampleJson);
        Document<?> result = unifiedParser.parse(jsonReader, "application/json", options);
        assertNotNull("Parsed document should not be null", result);
        assertTrue("Root should be an instance of Entry", result.getRoot() instanceof Entry);
        assertEquals("entry", result.getRoot().getQName().getLocalPart());

        WriterFactory writerFactory = new Abdera().getWriterFactory();
        Writer writer = writerFactory.getWriter("prettyxml");
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(result, stringWriter);
        System.out.println("Parsed JSON Entry as Abdera Entry:\n" + stringWriter);
    }

    @Test
    public void testValidXmlReader() throws IOException {
        Reader xmlReader = new StringReader(sampleXml);
        Document<Element> result = unifiedParser.parse(xmlReader, "application/atom+xml", options);
        assertNotNull(result);
        assertEquals("entry", result.getRoot().getQName().getLocalPart());
        WriterFactory writerFactory = new Abdera().getWriterFactory();
        Writer writer = writerFactory.getWriter("prettyxml");
        StringWriter stringWriter = new StringWriter();
        writer.writeTo(result, stringWriter);
        System.out.println("Parsed XML Entry as Abdera Entry:\n" + stringWriter);
    }


    //test for passing empty json
    @Test(expected = Exception.class)
    public void testEmptyJson() throws IOException{
        InputStream jsonStream = new ByteArrayInputStream("".getBytes());
        unifiedParser.parse(jsonStream, "application/json", options);
    }


    @Test(expected = Exception.class)
    public void testEmptyXml() throws IOException{
        InputStream xmlStream = new ByteArrayInputStream("".getBytes());
        unifiedParser.parse(xmlStream, "application/atom+xml", options);
    }


    @Test(expected = Exception.class)
    public void testEmptyJsonReaderInput() throws IOException{
        Reader jsonReader = new StringReader("");
        unifiedParser.parse(jsonReader, "application/json", options);
    }

    @Test(expected = Exception.class)
    public void testEmptyXmlReaderInput() throws IOException{
        Reader xmlReader = new StringReader("");
        unifiedParser.parse(xmlReader, "application/atom+xml", options);
    }

    @Test(expected = Exception.class)
    public void testHeaderJsonButBodyXml() throws IOException {
        InputStream stream = new ByteArrayInputStream(sampleXml.getBytes());
        unifiedParser.parse(stream, "application/json", options);
    }

    @Test(expected = Exception.class)
    public void testHeaderXmlButBodyJson() throws IOException {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        unifiedParser.parse(stream, "application/atom+xml", options);
    }

    @Test(expected = Exception.class)
    public void testReaderHeaderJsonButBodyXml() throws IOException {
        Reader reader = new StringReader(sampleXml);
        unifiedParser.parse(reader, "application/json", options);
    }

    @Test(expected = Exception.class)
    public void testReaderHeaderXmlButBodyJson() throws IOException {
        Reader reader = new StringReader(sampleJson);
        unifiedParser.parse(reader, "application/atom+xml", options);
    }

    @Test(expected = Exception.class)
    public void testNoContentType() throws IOException {
        InputStream stream = new ByteArrayInputStream(sampleJson.getBytes());
        unifiedParser.parse(stream, null, options);
    }

    @Test(expected = Exception.class)
    public void testReaderNoContentType() throws IOException {
        Reader reader = new StringReader(sampleJson);
        unifiedParser.parse(reader, null, options);
    }







}

