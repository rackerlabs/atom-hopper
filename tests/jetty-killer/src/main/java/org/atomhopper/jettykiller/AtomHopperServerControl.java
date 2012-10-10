package org.atomhopper.jettykiller;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class AtomHopperServerControl {
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServerControl.class);

    private final CommandLineArguments commandLineArgs;
    private static final String LOCALHOST_IP = "127.0.0.1";

    public AtomHopperServerControl(CommandLineArguments commandLineArgs) {
        this.commandLineArgs = commandLineArgs;
    }

    public void stopAtomHopper() {
        try {
            Socket s = new Socket(InetAddress.getByName(LOCALHOST_IP), commandLineArgs.stopport);
            OutputStream out = s.getOutputStream();
            LOG.info("Sending Atom Hopper stop request");
            out.write(("\r\n").getBytes());
            out.flush();
            s.close();
        } catch (IOException ioex) {
            LOG.error("An error occured while attempting to stop Atom Hopper: " + ioex.getMessage());
        }
    }
}
