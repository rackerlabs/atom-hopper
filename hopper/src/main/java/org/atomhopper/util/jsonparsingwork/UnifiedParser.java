package org.atomhopper.util.jsonparsingwork;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UnifiedParser implements Parser {

    private final Parser xmlParser;
    private final JsonAtomParser jsonParser;
    private ParserOptions defaultParserOptions;

    static Logger LOG = LoggerFactory.getLogger(UnifiedParser.class);

    public UnifiedParser(Parser xmlParser) {
        if (xmlParser == null) {
            LOG.error("XML parser is null in constructor");
            throw new IllegalArgumentException("XML parser cannot be null");
        }
        this.xmlParser = xmlParser;
        this.jsonParser = new JsonAtomParser();
        this.defaultParserOptions = xmlParser.getDefaultParserOptions();
        LOG.info("UnifiedParser is initialised with XMLParser: {}", xmlParser.getClass().getSimpleName());
    }

    private boolean isJsonContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase();
        return normalized.contains("json") ||
                normalized.contains("javascript") ||
                normalized.contains("text/json");
    }

    private boolean isXmlContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase();
        return normalized.contains("xml") ||
                normalized.contains("atom") ||
                normalized.contains("application/xml") ||
                normalized.contains("text/xml");
    }

    private boolean isJsonContent(ParserOptions options) {
        // Since ParserOptions doesn't have getMimeType(), we need a different approach
        // This could be:
        // 1. Check a custom property/attribute if your ParserOptions implementation supports it
        // 2. Use a custom extended ParserOptions interface
        // 3. Pass content type separately as a parameter
        // For now, return false as default behavior
        return false;
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, ParserOptions options) throws ParseException {
        if (in == null) {
            LOG.warn("InputStream is null in parse() method");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            // Default to XML parsing since we can't determine content type from ParserOptions
            return xmlParser.parse(in,null, options);
        } catch (Exception e) {
            Exception exception = e;
            if (!(exception instanceof ParseException)) {
                exception = new ParseException(exception);
            }
            throw (ParseException) exception;
        }
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, String base) throws ParseException {
        return parse(in, base, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, String base, ParserOptions options) throws ParseException {
        if (in == null) {
            LOG.warn("InputStream is null in parse() method with ParserOptions" );
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            if (isJsonContent(base)) {
                LOG.debug("Routing to JSON parser for base: {}", base);
                // Route to JSON parser
                return jsonParser.parse(in, base, options);
            }
            else {
                LOG.debug("Routing to XML parser for base: {}", base);
                return xmlParser.parse(in, base, options);
            }
        } catch (Exception e) {
            LOG.error("Parsing failed for base: {} with error: {}", base, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, String base) throws ParseException {
        return parse(reader, base, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, String base, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader cannot be null");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            if (isJsonContent(base)) {
                LOG.debug("Routing to JSON parser for base: {}", base);
                return jsonParser.parse(reader, base, options);
            } else {
                LOG.debug("Routing to XML parser for base: {}", base);
                return xmlParser.parse(reader, base, options);
            }
        } catch (Exception e) {
            LOG.error("Parsing failed for base: {} with error: {}", base, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(XMLStreamReader reader) throws ParseException {
        return parse(reader, null, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(XMLStreamReader reader, String base, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader cannot be null, line 132");
            throw new IllegalArgumentException("XMLStreamReader cannot be null");
        }


        try {
            LOG.debug("Routing to XML parser for base : {}", base);
            return xmlParser.parse(reader, base, options);
        } catch (Exception e) {
            LOG.error("Parsing failed for base: {} with error: {}", base, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in) throws ParseException {
        return parse(in, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader) throws ParseException {
        return parse(reader, null, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, ParserOptions options) throws ParseException {
        return parse(reader, null, options);
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
            LOG.warn("ReadableByteChannel cant be bull , line 179");
            throw new IllegalArgumentException("ReadableByteChannel cannot be null");
        }

        try {
            // Default to XML parsing since we can't determine content type from ParserOptions
            LOG.debug("Routing to XML parser for base : {}", base);
            return xmlParser.parse(ch, base, options);
        } catch (Exception e) {
            LOG.error("Parsing failed for base: {} with error: {}", base, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public <T extends Element> Document<T> parse(Reader reader, String contentType, String base) throws ParseException {
        return parse(reader, contentType, base, getDefaultParserOptions());
    }

    public <T extends Element> Document<T> parse(Reader reader, String contentType, String base, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader is null");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Using JSON parser for content type: {}", contentType);
                return jsonParser.parse(reader, base, options);
            } else if (isXmlContent(contentType)) {
                LOG.debug("Using XML parser for content type: {}", contentType);
                return xmlParser.parse(reader, base, options);
            } else {
                LOG.warn("Unknown content type: {}. Defaulting to XML parser", contentType);
                return xmlParser.parse(reader, base, options);
            }
        } catch (Exception e) {
            LOG.error("Parsing failed for content type: {}", contentType, e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    @Override
    public ParserOptions getDefaultParserOptions() {
        return defaultParserOptions != null ? defaultParserOptions : xmlParser.getDefaultParserOptions();
    }

    @Override
    public Parser setDefaultParserOptions(ParserOptions parserOptions) {
        this.defaultParserOptions = parserOptions;
        return this;
    }

    // Custom methods that accept content type for proper JSON/XML routing
    public <T extends Element> Document<T> parseWithContentType(InputStream in, String contentType, ParserOptions options) throws ParseException {
        if (in == null) {
            LOG.warn("InputStream is null in parseWithContentType(InputStream, contentType)");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for contentType: {}", contentType);
                return (Document<T>) jsonParser.parse(in, options);
            } else {
                LOG.debug("Routing to XML parser for contentType: {}", contentType);
                return xmlParser.parse(in, options);
            }
        } catch (Exception e) {
            LOG.error("Error parsing InputStream with contentType {}: {}", contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public <T extends Element> Document<T> parseWithContentType(Reader reader, String contentType, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader is null in parseWithContentType(Reader, contentType)");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for contentType: {}", contentType);
                return (Document<T>) jsonParser.parse(reader, options);
            } else {
                LOG.debug("Routing to XML parser for contentType: {}", contentType);
                return xmlParser.parse(reader, options);
            }
        } catch (Exception e) {

            LOG.error("Error parsing Reader with contentType {}: {}", contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);

        }
    }

    public <T extends Element> Document<T> parseWithContentType(InputStream in, String base, String contentType, ParserOptions options) throws ParseException {
        if (in == null) {
            LOG.warn("InputStream is null in parseWithContentType(InputStream, base, contentType)");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for base: {}, contentType: {}", base, contentType);
                return jsonParser.parse(in, base, options);
            } else {
                LOG.debug("Routing to XML parser for base: {}, contentType: {}", base, contentType);
                return xmlParser.parse(in, base, options);
            }
        } catch (Exception e) {

            LOG.error("Error parsing InputStream with base {} and contentType {}: {}", base, contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public <T extends Element> Document<T> parseWithContentType(Reader reader, String base, String contentType, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader is null in parseWithContentType(Reader, base, contentType)");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for base: {}, contentType: {}", base, contentType);
                return jsonParser.parse(reader, base, options);
            } else {
                LOG.debug("Routing to XML parser for base: {}, contentType: {}", base, contentType);
                return xmlParser.parse(reader, base, options);
            }
        } catch (Exception e) {

            LOG.error("Error parsing Reader with base {} and contentType {}: {}", base, contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }
}