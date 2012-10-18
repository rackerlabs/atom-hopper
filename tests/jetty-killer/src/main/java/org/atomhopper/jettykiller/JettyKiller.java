package org.atomhopper.jettykiller;




import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JettyKiller {
    private static final Logger LOG = LoggerFactory.getLogger(JettyKiller.class);
    private static final int MAX_PORT_NUMBER = 49150;
    private static final int MIN_PORT_NUMBER = 1024;

    private JettyKiller(){}

    public static void main(String[] args) {
        CommandLineArguments commandLineArgs = new CommandLineArguments();
        CmdLineParser cmdLineParser = new CmdLineParser(commandLineArgs);
        AtomHopperServerControl serverControl = new AtomHopperServerControl(commandLineArgs);

        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            displayUsage(cmdLineParser, e);
            return;
        }

        if (!(portIsInRange(commandLineArgs.stopport))) {
            LOG.info("Invalid Atom Hopper port setting, use a value between 1 and 65535");
            return;
        }

        serverControl.stopAtomHopper();
    }

    private static void displayUsage(CmdLineParser cmdLineParser, Exception e) {
        System.err.println(e.getMessage());
        System.err.println("java -jar AtomHopperServer.jar [options...] arguments...");
        cmdLineParser.printUsage(System.err);
    }

    private static boolean portIsInRange(int portNum) {
        return (portNum <= MAX_PORT_NUMBER) && (portNum >= MIN_PORT_NUMBER);
    }
}
