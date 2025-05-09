package org.atomhopper.util.jsonparsingwork;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.Abdera;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.channels.ReadableByteChannel;

public class JsonAtomParser implements Parser {


    public Document<Element> parse(InputStream in, ParserOptions options)  {
        try{
            //converting the inputStream to POJO

            EntryJsonPOJO.Root entryPojo = JsonToPojo.fromInputStream(in);
            Entry entry = AbderaEntryBuilder.EntryBuiderMethod(entryPojo);

            Abdera abdera = new Abdera();

            //creating a bytestream to store xml
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //convert entry to xml and store in baos
            entry.writeTo(baos);

            //read the xml representation from the byte array
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return (Document<Element>) abdera.getParser().parse(bais);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse the Json input", e);
        }
    }

    public Document<Element> parse(Reader reader, ParserOptions options){
        try{
            EntryJsonPOJO.Root entryPojo = JsonToPojo.fromReader(reader);
            Entry entry = AbderaEntryBuilder.EntryBuiderMethod(entryPojo);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entry.writeTo(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Abdera abdera = new Abdera();
            return (Document<Element>) abdera.getParser().parse(bais);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse the JSON reader input.", e);
        }
    }

    //unused methods
    public <T extends Element> Document<T> parse(InputStream var1, String var2){ throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(Reader var1, String var2) {throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(XMLStreamReader r) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(InputStream in) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(Reader reader, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(ReadableByteChannel ch) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, String type) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, ParserOptions options) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(ReadableByteChannel ch, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(XMLStreamReader r, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(InputStream in, String type, ParserOptions options) { throw new UnsupportedOperationException(); }
    public <T extends Element> Document<T> parse(Reader reader) { throw new UnsupportedOperationException(); }
    public ParserOptions getDefaultParserOptions() { return null; }
    public org.apache.abdera.parser.Parser setDefaultParserOptions(ParserOptions parserOptions) { return null; }
}

