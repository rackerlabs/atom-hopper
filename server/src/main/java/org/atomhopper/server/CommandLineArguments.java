package org.atomhopper.server;

import org.kohsuke.args4j.*;

class CommandLineArguments {

    private static final String DEFAULT_PORT_INFO = "(Default is port 8080, range is 1024 to 49150)";
    static final String ACTION_START = "start";
    static final String ACTION_STOP = "stop";
    private static final int PORT = 8080;
    private static final int STOPPORT = 8818;

    @Option(name = "-p", aliases = {"--port"},
            usage = "Atom Hopper port number " + DEFAULT_PORT_INFO)
    public final Integer port = PORT;

    @Option(name = "-s", aliases = {"--shutdown-port"},
            usage = "The port used to communicate a shutdown to Atom Hopper " + DEFAULT_PORT_INFO)
    public final Integer stopport = STOPPORT;

    @Option(name = "-c", aliases = {"--config-file"},
            usage = "The location and name of the Atom Hopper configuration file")
    public String configFile;

    //Note: I recommend keeping this an argument to stay inline with what people expect from a daemon script
    @Argument(usage = "Action to take - start | stop", required = true)
    public final String action = ACTION_START;
}
