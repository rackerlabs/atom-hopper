package org.atomhopper.util.jsonparsingwork;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;

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
        LOG.info("UnifiedParser is initialized with XMLParser: {}", xmlParser.getClass().getSimpleName());
    }

    private boolean isJsonContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase().trim();
        return normalized.startsWith("application/json") ||
                normalized.startsWith("text/json") ||
                normalized.contains("+json");
    }

    private boolean isXmlContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase().trim();
        return normalized.startsWith("application/xml") ||
                normalized.startsWith("text/xml") ||
                normalized.startsWith("application/atom+xml") ||
                normalized.contains("+xml");
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            LOG.error("Content-Type header is missing or empty");
            throw new IllegalArgumentException("Content-Type header is required");
        }

        if (!isJsonContent(contentType) && !isXmlContent(contentType)) {
            LOG.error("Unsupported Content-Type: {}", contentType);
            throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, ParserOptions options) throws ParseException {
        if (in == null) {
            throw new ParseException("Input stream cannot be null");
        }
        try {
            if (!in.markSupported()) {
                in = new BufferedInputStream(in);
            }
            in.mark(1024);
            if (in.read() == -1) {
                throw new ParseException("Empty input stream");
            }
            in.reset();
            return xmlParser.parse(in, options);
        } catch (IOException e) {
            throw new ParseException("Error reading input stream", e);
        }
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, String base) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() or parseFromHttpRequest() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(InputStream in, String base, ParserOptions options) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() or parseFromHttpRequest() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, String base) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, String base, ParserOptions options) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(XMLStreamReader reader) throws ParseException {
        return parse(reader, null, getDefaultParserOptions());
    }

    @Override
    public <T extends Element> Document<T> parse(XMLStreamReader reader, String base, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("XMLStreamReader cannot be null");
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
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() or parseFromHttpRequest() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(Reader reader, ParserOptions options) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, String base) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, ParserOptions options) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    @Override
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, String base, ParserOptions options) throws ParseException {
        throw new UnsupportedOperationException("Content-Type header is required for parsing. Use parseWithContentType() methods instead.");
    }

    public <T extends Element> Document<T> parseWithContentType(InputStream in, String contentType, ParserOptions options) throws ParseException {
        validateContentType(contentType);

        if (in == null) {
            LOG.warn("InputStream is null");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for Content-Type: {}", contentType);
                return jsonParser.parse(in, options);
            } else {
                LOG.debug("Routing to XML parser for Content-Type: {}", contentType);
                return xmlParser.parse(in, options);
            }
        } catch (Exception e) {
            LOG.error("Error parsing with Content-Type {}: {}", contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public <T extends Element> Document<T> parseWithContentType(Reader reader, String contentType, ParserOptions options) throws ParseException {
        validateContentType(contentType);

        if (reader == null) {
            LOG.warn("Reader is null");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for Content-Type: {}", contentType);
                return jsonParser.parse(reader, options);
            } else {
                LOG.debug("Routing to XML parser for Content-Type: {}", contentType);
                return xmlParser.parse(reader, options);
            }
        } catch (Exception e) {
            LOG.error("Error parsing with Content-Type {}: {}", contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public <T extends Element> Document<T> parseWithContentType(InputStream in, String base, String contentType) throws ParseException {
        return parseWithContentType(in, base, contentType, getDefaultParserOptions());
    }

    public <T extends Element> Document<T> parseWithContentType(InputStream in, String base, String contentType, ParserOptions options) throws ParseException {
        validateContentType(contentType);

        if (in == null) {
            LOG.warn("InputStream is null");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for base: {}, Content-Type: {}", base, contentType);
                return jsonParser.parse(in, base, options);
            } else {
                LOG.debug("Routing to XML parser for base: {}, Content-Type: {}", base, contentType);
                return xmlParser.parse(in, base, options);
            }
        } catch (Exception e) {
            LOG.error("Error parsing with base {} and Content-Type {}: {}", base, contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public <T extends Element> Document<T> parseWithContentType(Reader reader, String base, String contentType) throws ParseException {
        return parseWithContentType(reader, base, contentType, getDefaultParserOptions());
    }

    public <T extends Element> Document<T> parseWithContentType(Reader reader, String base, String contentType, ParserOptions options) throws ParseException {
        validateContentType(contentType);

        if (reader == null) {
            LOG.warn("Reader is null");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for base: {}, Content-Type: {}", base, contentType);
                return jsonParser.parse(reader, base, options);
            } else {
                LOG.debug("Routing to XML parser for base: {}, Content-Type: {}", base, contentType);
                return xmlParser.parse(reader, base, options);
            }
        } catch (Exception e) {
            LOG.error("Error parsing with base {} and Content-Type {}: {}", base, contentType, e.getMessage(), e);
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
}