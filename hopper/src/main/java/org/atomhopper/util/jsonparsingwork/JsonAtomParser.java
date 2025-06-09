package org.atomhopper.util.jsonparsingwork;

import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.Abdera;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;

import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonAtomParser implements Parser {

    private ParserOptions defaultParserOptions;
    private final Abdera abdera;

    private static final Logger LOG = LoggerFactory.getLogger(JsonAtomParser.class);

public JsonAtomParser() {
        this.abdera = new Abdera();
        this.defaultParserOptions = null;
    }


    private <T extends Element> Document<T> parseJsonToDocument(EntryJsonPOJO.Root entryPojo) throws ParseException {
        try {
            // Convert JSON POJO directly to Abdera Entry
            Entry entry = AbderaEntryBuilder.EntryBuiderMethod(entryPojo);
            // Create a document using the Factory and set the entry as root
            Document<Entry> entryDocument = abdera.getFactory().newDocument();
            entryDocument.setRoot(entry);
            LOG.debug("Successfully created Abdera Document from POJO");

            return (Document<T>) entryDocument;
        } catch (Exception e) {
            LOG.error("Failed to convert JSON POJO to Abdera Document", e);
            throw new ParseException("Failed to convert JSON to Abdera Document", e);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, ParserOptions options) throws ParseException {
        if (in == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            // JSON InputStream -> POJO -> Document
            EntryJsonPOJO.Root entryPojo = JsonToPojo.fromInputStream(in);
            LOG.debug("Successfully parsed JSON to POJO");
            return parseJsonToDocument(entryPojo);
        } catch (IOException e) {
            LOG.error("IOException while parsing JSON from InputStream", e);
            throw new ParseException("Failed to parse JSON from InputStream", e);
        } catch (Exception e) {
            LOG.error("Unexpected error during JSON parsing", e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader is null in parse(Reader, ParserOptions)");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            // JSON Reader -> POJO -> Document
            LOG.debug("Parsing JSON from Reader");
            EntryJsonPOJO.Root entryPojo = JsonToPojo.fromReader(reader);
            LOG.debug("Successfully parsed JSON to POJO from Reader");
            return parseJsonToDocument(entryPojo);
        } catch (IOException e) {
            LOG.error("IOException while parsing JSON from Reader", e);
            throw new ParseException("Failed to parse JSON from Reader", e);
        } catch (Exception e) {
            LOG.error("Unexpected error during JSON parsing from Reader", e);
            if (!(e instanceof ParseException)) {
                e = new ParseException(e);
            }
            throw (ParseException) e;
        }
    }



    @Override
    public <T extends Element> Document<T> parse(InputStream in, String base) throws ParseException {
        return parse(in, base, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, String base, ParserOptions options) throws ParseException {
        // For JSON parsing, base URL doesn't affect the parsing logic significantly
        // but we maintain the interface compatibility
        return parse(in, options);
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, String base) throws ParseException {
        return parse(reader, base, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, String base, ParserOptions options) throws ParseException {
        return parse(reader, options);
    }

    @Override
    public <T extends Element> Document<T> parse(XMLStreamReader reader) throws ParseException {
        throw new ParseException("XMLStreamReader parsing is not supported for JSON content");
    }

    @Override
    public <T extends Element> Document<T> parse(XMLStreamReader reader, String base, ParserOptions options) throws ParseException {
        throw new ParseException("XMLStreamReader parsing is not supported for JSON content");
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in) throws ParseException {
        return parse(in, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader) throws ParseException {
        return parse(reader, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch) throws ParseException {
        return parse(ch, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, String base) throws ParseException {
        return parse(ch, base, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, ParserOptions options) throws ParseException {
        return parse(ch, null, options);
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, String base, ParserOptions options) throws ParseException {
        if (ch == null) {
            LOG.warn("ReadableByteChannel is null in parse(ReadableByteChannel, base, options)");
            throw new IllegalArgumentException("ReadableByteChannel cannot be null");
        }

        try {
            // Convert ReadableByteChannel to InputStream for JSON parsing
            LOG.debug("Converting ReadableByteChannel to InputStream for JSON parsing");
            InputStream inputStream = Channels.newInputStream(ch);
            return parse(inputStream, options);
        } catch (Exception e) {
            LOG.error("Error parsing from ReadableByteChannel with base {}: {}", base, e.getMessage(), e);
            if (!(e instanceof ParseException)) {
                e = new ParseException(e);
            }
            throw (ParseException) e;
        }
    }

    @Override
    public ParserOptions getDefaultParserOptions() {
        return defaultParserOptions;
    }

    @Override
    public Parser setDefaultParserOptions(ParserOptions parserOptions) {
        this.defaultParserOptions = parserOptions;
        return this;
    }
}