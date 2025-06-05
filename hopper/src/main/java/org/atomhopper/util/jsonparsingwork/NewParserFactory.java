package org.atomhopper.util.jsonparsingwork;

import org.apache.abdera.parser.Parser;

public class NewParserFactory {

    private Parser unifiedParser;

    public void setUnifiedParser(Parser unifiedParser){
        this.unifiedParser = unifiedParser;
    }

    public Parser getParser(){
        return unifiedParser;
    }
}
