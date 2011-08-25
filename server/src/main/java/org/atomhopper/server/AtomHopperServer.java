package org.atomhopper.server;


import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AtomHopperServer {
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServer.class);
    
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

        if (commandLineArgs != null) {
            if( (!(portIsInRange(commandLineArgs.port))) || (!(portIsInRange(commandLineArgs.stopport))) ) {
                LOG.info("Invalid Atom Hopper port setting, use a value between 1024 and 49150");
                return;
            }
            
            if (commandLineArgs.action.equalsIgnoreCase(commandLineArgs.ACTION_START))
                serverControl.startAtomHopper();
            if (commandLineArgs.action.equalsIgnoreCase(commandLineArgs.ACTION_STOP))
                serverControl.stopAtomHopper();
        }
    }

    private static void displayUsage(CmdLineParser cmdLineParser, Exception e) {
        System.err.println(e.getMessage());
        System.err.println("java -jar AtomHopperServer.jar [options...] arguments...");
        cmdLineParser.printUsage(System.err);
    }
    
    private static boolean portIsInRange(int portNum) {
        if((portNum < 49150) && (portNum > 1024)) {
            return true;
        }
        return false;
    }
}
