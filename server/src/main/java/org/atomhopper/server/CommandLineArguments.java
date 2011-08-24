package org.atomhopper.server;


import org.kohsuke.args4j.*;


public class CommandLineArguments {
    
    @Option(name = "-port", usage = "Atom Hopper port number (Default is port 8080)")
    public Integer port = 8080;
    
    @Option(name="-stopport", usage = "The port used to comminicate a shutdown to Atom Hopper (Default is port 8818)")
    public Integer stopport = 8818;
    
    @Option(name="-action", usage = "The action regarding Atom Hopper: start or stop (Default is 'start')")
    public String action = "start";
    
}
