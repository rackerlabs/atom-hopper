package org.atomhopper.jettykiller;



import org.kohsuke.args4j.Option;

public class CommandLineArguments {

    private static final String DEFAULT_PORT_INFO = "(Default is port 8818, range is 1024 to 49150)";
    private static final int STOPPORT = 8818;

    @Option(name = "-s", aliases = {"--shutdown-port", "-p", "--port"},
            usage = "The port used to communicate a shutdown to Atom Hopper " + DEFAULT_PORT_INFO)
    public final Integer stopport = STOPPORT;

}
