package org.atomhopper.server;

import org.kohsuke.args4j.*;

public class CommandLineArguments {

    @Option(name = "-p", aliases = {"--port"}, usage = "Atom Hopper port number (Default is port 8080)")
    public Integer port = 8080;
    
    @Option(name = "-s", aliases = {"--shutdown-port"}, usage = "The port used to comminicate a shutdown to Atom Hopper (Default is port 8818)")
    public Integer stopport = 8818;
    
    //Note: I recommend keeping this an argument to stay inline with what people expect from a daemon script
    @Argument(usage = "Action to take - start | stop", required = true)
    public String action = "start";
}
