package org.atomhopper.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStream;
import org.atomhopper.jetty.AtomHopperJettyServerBuilder;
import org.eclipse.jetty.server.Server;
import org.kohsuke.args4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AtomHopperServer 
{
    private static Server serverInstance;
    private static CommandLineArguments commandLineArgs;
    private static CmdLineParser cmdLineParser;   
    
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServer.class);
    
    public static void main( String[] args )
    {
        commandLineArgs = new CommandLineArguments();
        cmdLineParser = new CmdLineParser(commandLineArgs);
            
        try {
            cmdLineParser.parseArgument(args);
        }
        catch(CmdLineException e) {
            displayUsage(e);
            return;
        }

        if(commandLineArgs != null) {
            if(commandLineArgs.action.equalsIgnoreCase("start")) {
                startAtomHopper();
            }
            else {
                stopAtomHopper();
            }
        }
    }
    
    private static void displayUsage(Exception e) {
        System.err.println(e.getMessage());
        System.err.println("java -jar myprogram.jar [options...] arguments...");
        cmdLineParser.printUsage(System.err);        
    }
    
    public static void startAtomHopper() {
            
        try {
            serverInstance = new AtomHopperJettyServerBuilder(getPort()).newServer();
            serverInstance.setStopAtShutdown(true);
            serverInstance.start();
            Thread monitor = new MonitorThread();
            monitor.start();
            LOG.info("Atom Hopper running and listening on port: " + Integer.toString(commandLineArgs.port) + "\n"); 
        }
        catch(Exception e) {
            LOG.error("Atom Hopper could not be started: " + e.getMessage());
        } 
    }
    
    public static void stopAtomHopper() {
        try {
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), commandLineArgs.stopport);
            OutputStream out = s.getOutputStream();
            System.out.println("Sending Atom Hopper stop request");
            out.write(("\r\n").getBytes());
            out.flush();
            s.close();
        }
        catch(IOException ioex) {
            LOG.error("An error occured while attempting to stop Atom Hopper: " + ioex.getMessage());
        }
    }
            
    
    private static int getPort() {
        return commandLineArgs.port;
    }
    
    private static int getStopPort() {
        return commandLineArgs.stopport;
    }    
    
    private static class MonitorThread extends Thread {

        private ServerSocket socket;

        public MonitorThread() {
            setDaemon(true);
            setName("StopMonitor");
            try {
                socket = new ServerSocket(getStopPort(), 1, InetAddress.getByName("127.0.0.1"));
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            Socket accept;
            String userInput;
            
            try {
                accept = socket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                reader.readLine();                
                LOG.info("Stopping Atom Hopper...");
                serverInstance.stop();
                LOG.info("Atom Hopper has been stopped");
                accept.close();
                socket.close();
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }  
}
