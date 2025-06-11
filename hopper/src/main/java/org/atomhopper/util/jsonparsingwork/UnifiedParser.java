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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UnifiedParser implements Parser {

    private final Parser xmlParser;
    private final JsonAtomParser jsonParser;
    private ParserOptions defaultParserOptions;

    // Supported JSON content types
    private static final Set<String> SUPPORTED_JSON_TYPES = new HashSet<>(Arrays.asList(
            "application/json",
            "application/atom+json",
            "text/json"
    ));

    // Supported XML content types
    private static final Set<String> SUPPORTED_XML_TYPES = new HashSet<>(Arrays.asList(
            "application/xml",
            "application/atom+xml",
            "text/xml"
    ));

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


    private boolean isJsonContent(String contentType) throws ParseException {
        validateContentType(contentType);
        String normalized = normalizeContentType(contentType);
        return SUPPORTED_JSON_TYPES.contains(normalized);
    }

    private boolean isXmlContent(String contentType) throws ParseException {
        validateContentType(contentType);
        String normalized = normalizeContentType(contentType);
        return SUPPORTED_XML_TYPES.contains(normalized);
    }

    private void validateContentType(String contentType) throws ParseException {
        if (contentType == null || contentType.trim().isEmpty()) {
            LOG.error("Content-Type header is missing or empty");
            throw new ParseException("Content-Type header is required and cannot be null or empty");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }

        String normalized = contentType.toLowerCase().split(";")[0].trim();
        return normalized;
    }


    private void validateSupportedContentType(String contentType) throws ParseException {
        validateContentType(contentType);
        String normalized = normalizeContentType(contentType);

        if (!SUPPORTED_JSON_TYPES.contains(normalized) && !SUPPORTED_XML_TYPES.contains(normalized)) {
            LOG.error("Unsupported Content-Type: {}", contentType);
            throw new ParseException("Unsupported Content-Type: " + contentType +
                    ". Supported types are: " + SUPPORTED_JSON_TYPES + ", " + SUPPORTED_XML_TYPES);
        }
    }

    // Legacy methods that don't have content type - these will default to XML for backward compatibility
    // but log warnings about missing content type

    @Override
    public <T extends Element> Document<T> parse(InputStream in, ParserOptions options) throws ParseException {
        if (in == null) {
            LOG.warn("InputStream is null in parse() method");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        LOG.warn("Parsing without Content-Type header - defaulting to XML parser. Consider using parseWithContentType() method.");

        try {
            return xmlParser.parse(in, null, options);
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
            LOG.warn("InputStream is null in parse() method with ParserOptions");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        LOG.warn("Parsing without Content-Type header - defaulting to XML parser. Consider using parseWithContentType() method.");

        try {
            return xmlParser.parse(in, base, options);
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

        LOG.warn("Parsing without Content-Type header - defaulting to XML parser. Consider using parseWithContentType() method.");

        try {
            return xmlParser.parse(reader, base, options);
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
            LOG.warn("Reader cannot be null");
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
            LOG.warn("ReadableByteChannel cant be null");
            throw new IllegalArgumentException("ReadableByteChannel cannot be null");
        }

        LOG.warn("Parsing without Content-Type header - defaulting to XML parser. Consider using parseWithContentType() method.");

        try {
            return xmlParser.parse(ch, base, options);
        } catch (Exception e) {
            LOG.error("Parsing failed for base: {} with error: {}", base, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    // Updated method that validates content type before parsing
    public <T extends Element> Document<T> parse(Reader reader, String contentType, String base) throws ParseException {
        return parse(reader, contentType, base, getDefaultParserOptions());
    }

    public <T extends Element> Document<T> parse(Reader reader, String contentType, String base, ParserOptions options) throws ParseException {
        if (reader == null) {
            LOG.warn("Reader is null");
            throw new IllegalArgumentException("Reader cannot be null");
        }

        // Validating content type first
        validateSupportedContentType(contentType);

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Using JSON parser for content type: {}", contentType);
                return jsonParser.parse(reader, base, options);
            } else if (isXmlContent(contentType)) {
                LOG.debug("Using XML parser for content type: {}", contentType);
                return xmlParser.parse(reader, base, options);
            } else {
                // This should not happen due to validation above, but keeping for safety
                LOG.error("Unsupported content type after validation: {}", contentType);
                throw new ParseException("Unsupported content type: " + contentType);
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

    // Primary methods that require content type for proper JSON/XML routing
    public <T extends Element> Document<T> parseWithContentType(InputStream in, String contentType, ParserOptions options) throws ParseException {
        if (in == null) {
            LOG.warn("InputStream is null in parseWithContentType(InputStream, contentType)");
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        // Validating content type first
        validateSupportedContentType(contentType);

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for contentType: {}", contentType);
                return (Document<T>) jsonParser.parse(in, options);
            } else if (isXmlContent(contentType)) {
                LOG.debug("Routing to XML parser for contentType: {}", contentType);
                return xmlParser.parse(in, options);
            } else {
                // This should not happen due to validation above, but keeping for safety
                LOG.error("Unsupported content type after validation: {}", contentType);
                throw new ParseException("Unsupported content type: " + contentType);
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

        // Validating content type first
        validateSupportedContentType(contentType);

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for contentType: {}", contentType);
                return (Document<T>) jsonParser.parse(reader, options);
            } else if (isXmlContent(contentType)) {
                LOG.debug("Routing to XML parser for contentType: {}", contentType);
                return xmlParser.parse(reader, options);
            } else {
                // This should not happen due to validation above, but keeping for safety
                LOG.error("Unsupported content type after validation: {}", contentType);
                throw new ParseException("Unsupported content type: " + contentType);
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

        // Validate content type first
        validateSupportedContentType(contentType);

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for base: {}, contentType: {}", base, contentType);
                return jsonParser.parse(in, base, options);
            } else if (isXmlContent(contentType)) {
                LOG.debug("Routing to XML parser for base: {}, contentType: {}", base, contentType);
                return xmlParser.parse(in, base, options);
            } else {
                // This should not happen due to validation above, but keeping for safety
                LOG.error("Unsupported content type after validation: {}", contentType);
                throw new ParseException("Unsupported content type: " + contentType);
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

        // Validating content type first
        validateSupportedContentType(contentType);

        try {
            if (isJsonContent(contentType)) {
                LOG.debug("Routing to JSON parser for base: {}, contentType: {}", base, contentType);
                return jsonParser.parse(reader, base, options);
            } else if (isXmlContent(contentType)) {
                LOG.debug("Routing to XML parser for base: {}, contentType: {}", base, contentType);
                return xmlParser.parse(reader, base, options);
            } else {
                // This should not happen due to validation above, but keeping for safety
                LOG.error("Unsupported content type after validation: {}", contentType);
                throw new ParseException("Unsupported content type: " + contentType);
            }
        } catch (Exception e) {
            LOG.error("Error parsing Reader with base {} and contentType {}: {}", base, contentType, e.getMessage(), e);
            throw (e instanceof ParseException) ? (ParseException) e : new ParseException(e);
        }
    }

    public Set<String> getSupportedJsonContentTypes() {
        return new HashSet<>(SUPPORTED_JSON_TYPES);
    }

    public Set<String> getSupportedXmlContentTypes() {
        return new HashSet<>(SUPPORTED_XML_TYPES);
    }
}