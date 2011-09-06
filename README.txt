ATOM Hopper - The Java ATOMpub Server Framework

The AtomHopperServer.jar (this is the Jetty embedded version) currently takes the following arguments:

The first two arguments are not optional and must be set to either 'start' or 'stop' Atom Hopper: start | stop

Example: java -jar AtomHopperServer.jar start

-p (alias --port)
This specifies the port number that Atom Hopper will listen to for incoming requests, if not set it will use a default port of 8080

Example: -p 8088
- this would specify that Atom Hopper listen to port 8088 for incoming requests

-s (alias --shutdown-port)
This is the port used to communicate a shutdown request to the Atom Hopper Server, if not set it will use a default port of 8818

Example: -s 8090
- this would specify that Atom Hopper should listen to port 8090 for the shutdown command

-c (alias --config-file)
This is the location and name of the Atom Hopper configuration file, if not set it will use default settings

Example: -c file:///Users/joeatom/atomhopper/atomhopper.cfg.xml


Notes Regarding licensing

All files contained with this distribution of ATOM Hopper are licenced 
under the Apache License v2.0 (http://www.apache.org/licenses/LICENSE-2.0).
You must agree to the terms of this license and abide by them before
viewing, utilizing or distributing the source code contained within this distribution.
