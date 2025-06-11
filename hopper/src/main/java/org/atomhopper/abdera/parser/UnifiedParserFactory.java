package org.atomhopper.abdera.parser;

import org.apache.abdera.parser.Parser;

public class UnifiedParserFactory {

    private Parser unifiedParser;

    public void setUnifiedParser(Parser unifiedParser){
        this.unifiedParser = unifiedParser;
    }

    public Parser getParser(){
        return unifiedParser;
    }
}
