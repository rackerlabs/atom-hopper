package org.atomhopper.util.jsonparsingwork;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;

public class UnifiedParser implements Parser {

    private final Parser xmlparser = new org.apache.abdera.parser.stax.FOMParser();
    private final JsonAtomParser jsonParser = new JsonAtomParser() ;


    public Document<Element> parse(InputStream in, String contentType, ParserOptions options){
        if(contentType != null && contentType.toLowerCase().contains("json")){
            return jsonParser.parse(in, options);
        }
        else{
            return xmlparser.parse(in, options);
        }
    }

    public Document<Element> parse(Reader reader, String ContentType, ParserOptions options){
        if(ContentType != null && ContentType.toLowerCase().contains("json")){
            return jsonParser.parse(reader, options);
        }
        else{
            return xmlparser.parse(reader, options);
        }
    }

    @Override public <T extends Element> Document<T> parse(InputStream in, ParserOptions options) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(InputStream var1, String var2){ throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(Reader var1, String var2) {throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(XMLStreamReader r) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(InputStream in) { throw new UnsupportedOperationException(); }
    //@Override public <T extends Element> Document<T> parse(InputStream in, ParserOptions options) { throw new UnsupportedOperationException(); }
    //@Override public <T extends Element> Document<T> parse(InputStream in, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(Reader reader) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(Reader reader, ParserOptions options) { throw new UnsupportedOperationException(); }
    //@Override public <T extends Element> Document<T> parse(Reader reader, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(ReadableByteChannel ch) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(ReadableByteChannel ch, String type) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(ReadableByteChannel ch, ParserOptions options) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(ReadableByteChannel ch, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    @Override public <T extends Element> Document<T> parse(XMLStreamReader r, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    @Override public ParserOptions getDefaultParserOptions() { return null; }
    @Override public Parser setDefaultParserOptions(ParserOptions parserOptions) { return this; }
}
