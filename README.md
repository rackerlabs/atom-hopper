<pre>
    ___   __                     __  __                            
   /   | / /_____  ____ ___     / / / /____  ____  ____  ___  _____
  / /| |/ __/ __ \/ __ `__ \   / /_/ // __ \/ __ \/ __ \/ _ \/ ___/
 / ___ / /_/ /_/ / / / / / /  / __  // /_/ / /_/ / /_/ /  __/ /    
/_/  |_\__/\____/_/ /_/ /_/  /_/ /_/ \____/ .___/ .___/\___/_/     
                                         /_/   /_/                 
</pre>

#ATOM Hopper - The Java ATOMPub Server#

Atom Hopper is a framework for accessing, processing, aggregating and indexing Atom formatted events. Atom Hopper was designed to make it easy to build both generalized and specialized persistence mechanisms for Atom XML data, based on the Atom Syndication Format and the Atom Publishing Protocol.

Benefits:

* Simple. Atom Hopper is easy to use. It can be used out-of-the-box as an executable JAR (running within an embedded Jetty Server). For more flexibility, it can be deployed as a WAR file into any Servlet container (ie: Tomcat, Jetty, etc.). Most applications can use Atom Hopper with minimal configuration to specify the Atom Workspaces and the Content storage.
* Scalable. Atom Hopper is very scalable because it is designed to be stateless, allowing state to be distributed across the web.
* Layered. Atom Hopper allows any number of intermediaries, such as proxies, gateways, and firewalls so one can easily layer aspects such as Security, Compression, etc. on an as needed basis.
* Built on a strong foundation. It is built on top of several open source projects such as Apache Abdera (a Java-based Atom Publishing framework) and Hibernate.
* Flexible. Atom Hopper currently supports the following relational databases: H2, PostgresSQL, and MySQL (plus others that work with Hibernate).
* High performance. Atom Hopper can handle high loads with high accuracy.
* Improving. Atom Hopper is under development and actively being worked on.

You can install/run Atom Hopper by several methods:

* Embedded version (for testing and evaluation purposes only)
* via a WAR file (recommended)
* via the source code (JAR)
* via Resource Package Manager (RPM)
* via Debian Package (DEB)

###Embedded Installation Method###

The embedded version of Atom Hopper is completely self-contained and requires nothing more than Oracle's JRE or the Open JDK version 1.6. Please keep in mind though that the embedded version is meant to be used to quickly get Atom Hopper up and running for testing and evaluation purposes.  For production environments you should use the WAR or RPM version of Atom Hopper.

The AtomHopperServer.jar (this is the Jetty embedded version) currently takes the following arguments:

The first two arguments are not optional and must be set to either: **start** | **stop**

<pre><code>
Example: java -jar AtomHopperServer.jar start
</code></pre>

###Arguments###

<pre><code>-p (alias --port)</code></pre>
This specifies the port number that Atom Hopper will listen to for incoming requests, if its not set it will use the default port of 8080

<pre><code>Example: -p 8088</code></pre>
This would specify that Atom Hopper listen to port 8088 for incoming requests

<pre><code>-s (alias --shutdown-port)</code></pre>
This is the port used to communicate a shutdown request to the Atom Hopper server, if its not set it will use a default port of 8818

<pre><code>Example: -s 8090</code></pre>
This would specify that Atom Hopper should listen to port 8090 for the shutdown command

<pre><code>-c (alias --config-file)</code></pre>
This is the location and name of the Atom Hopper configuration file, if not set it will use the default settings.

<pre><code>Example: -c file:///Users/joeatom/atomhopper/atomhopper.cfg.xml</code></pre>

###WAR Installation Method###

You may take the Atom Hopper WAR file and deploy it to an HTTP server/servlet container such as Apache Tomcat, Eclipse Jetty, Glassfish, etc.  Atom Hopper has been tested on Apache Tomcat and Eclipse Jetty.  Follow the instructions of the HTTP server/servlet container and deploy the Atom Hopper WAR file just as you would any other application.

###RPM Installation Method###

**Note:** To build the actual RPM file you need to be on a Fedora or CentOS system.

With this method, a single yum command installs Atom Hopper.

Required operating environment:

* A supported operating system:
        CentOS 6.0 or higher
        Fedora 15 or higher
* Java OpenJDK 1.6 (or Oracle's JRE) must be installed before running the RPM file
* Apache Tomcat 6 must be installed before running the RPM file

Run from the command prompt:
<pre><code>$ yum install ah-war-VERSION-INFO-HERE.noarch.rpm</code></pre>

After successfully completing the yum installation process you will have these files in the following locations:

Atom Hopper WAR: **/var/lib/tomcat6/webapps**

Atom Hopper H2 database: **/opt/atomhopper**

Config file for setting up namespaces and feeds: **/etc/atomhopper/atom-server.cfg.xml**

Config file for setting up the default data adapter: **/etc/atomhopper/application-context.xml**

**Note:** The Atom Hopper RPM is not signed so you might need to override the warning that yum issues when attempting to install the RPM file.

###Debian Installation Method###

With this method, a single dpkg command installs Atom Hopper.

Required operating environment:
* A supported operating system:
        Debian 6.0
* Oracle's 1.6 JRE must be installed before running the DEB file
* Apache Tomcat 6 must be installed before running the DEB file

Run from the command prompt:
<code><pre>$ dpkg -i install ah-war-VERSION-INFO-HERE.deb</code></pre>

After successfully completing the Debian package installation process you will have these files in the following locations:

Atom Hopper WAR: **/var/lib/tomcat6/webapps**

Atom Hopper H2 database: **/opt/atomhopper**

Config file for setting up namespaces and feeds: **/etc/atomhopper/atom-server.cfg.xml**

###Query parameters###

<table>
  <tr>
    <th>Paramter Name</th><th>Description</th><th>Data Type/Acceptable Values</th><th>Default</th>
  </tr>
  <tr>
    <td>marker</td><td>The unique id of an ATOM entry</td><td>Valid ID</td><td>(none)</td>
  </tr>
  <tr>
    <td>direction</td><td>The direction from the current marker (or entry) to start getting more entries from</td><td>forward | backward</td><td>forward</td>
  </tr>
  <tr>
    <td>limit</td><td>How many entries are returned. If the entered limit is greater than the actual number of entries, the actual number of entries will be used.</td><td>0 to n</td><td>25</td>  
  </tr>
  <tr>
      <td>format</td><td>Returns the feed in JSON format</td><td>json</td><td>(none)</td>
  </tr>
</table>

An HTTP/HTTPS POST is used to insert new ATOM entries into Atom Hopper.

###Adding a New Entry###

The following is an example of a simple ATOM entry (with three categories):

```xml
<entry xmlns="http://www.w3.org/2005/Atom">
  <title type="text">This is the title</title>
  <updated>2011-09-22T21:39:49.904Z</updated>
  <author>
    <name>John Doe</name>
  </author>
  <content type="text">Hello World</content>
  <category term="MyCategory 1" />
  <category term="MyCategory 2" />
  <category term="MyCategory 3" />
</entry>
```

The ATOM XML is sent to Atom Hopper via an HTTP POST along with the following HTTP Header:

</code></pre>Content-Type: application/atom+xml</code></pre>

###HTTP/HTTPS###

Depending on whether you use HTTP or HTTPS will determine how your self referencing ATOM entry links will appear.

With HTTPS POST:
```
<link href="https://domain.com/namespace/feed/entries/urn:uuid:9b850562-d357-4cf8-8811-048a6730e869" rel="self" />
```

With HTTP POST:
```
<link href="http://domain.com/namespace/feed/entries/urn:uuid:9b850562-d357-4cf8-8811-048a6730e869" rel="self" />
```

**Adding One or More Categories**

To add categories to an ATOM entry you need to include the category element along with a term attribute:

```xml
<category term="mycategory1" />
```

**Multiple categories**

```xml
<category term="mycategory1" />
<category term="mycategory2" />
```

###GET###

An HTTP GET is used to retrieve ATOM entries from Atom Hopper. The HTTP GET request supports asking for a specific entry, forward and backward paging, category search, and JSON feed returns.
<pre><code>
 http://localhost:8080/namespace/feed/ 
</code></pre>

###Select Entries by Marker###

**Note:** The uuid keyword is the unique ID of the entry.

The direction parameter is either forward or backward.  If the marker is used then the direction must be specified as well.
<pre><code>
 http://localhost:8080/namespace/feed/?marker=urn:uuid:8439541b-b40e-4c23-b290-2820bd64257d&direction=forward 

 http://localhost:8080/namespace/feed/?marker=urn:uuid:8439541b-b40e-4c23-b290-2820bd64257d&direction=backward 
</code></pre>

###Forward and Backward Paging###

The limit parameter may be used to specify the number of entries to return (by default this is set to 25).

<pre><code>
 http://localhost:8080/namespace/feed/?marker=urn:uuid:8439541b-b40e-4c23-b290-2820bd64257d&direction=forward&limit=50 

 http://localhost:8080/namespace/feed/?marker=urn:uuid:8439541b-b40e-4c23-b290-2820bd64257d&direction=backward&limit=50 
</code></pre>

###Using Entries###

Entries return one ATOM XML entry, it can be used to return a specific entry by itself.

<pre><code>
 http://localhost:8080/namespace/feed/entries/urn:uuid:8439541b-b40e-4c23-b290-2820bd64257d 
</code></pre>

###Filter Entries by Category###

**Single category:**

The entry category must be an exact match and is (currently) case sensitive. No wild cards are currently supported.  The following is an example of searching on a category entered as CAT1.

<pre><code>
 http://localhost:8080/namespace/feed/?search=%2BCAT1 
</code></pre>

**Note:** The %2B is the urlencoded value of the **+** operator.

**Multiple categories:**

To perform a search on multiple categories simply append additional categories examine the following example that search for two categories (CAT1 and CAT5):

<pre><code>
 http://localhost:8080/namespace/feed/?search=%2BCAT1%2BCAT5 
</code></pre>

###JSON Feed Returns###

You can receive a feed back in JSON format by doing the following:

<pre><code>
http://localhost:8080/namespace/feed/?format=json
</code></pre>

**Note:** If you do want to embed JSON into an ATOM XML entry then make sure to wrap the JSON content in an XML CDATA section.

###Weak eTag Support###

Atom Hopper supports weak eTags.  Weak eTags are sent back in the HTTP header with a name of Etag.  A weak eTag for a feed containing more than one ATOM entry looks like this:
<code><pre>
W/"urn:uuid:21d39ce9-940b-4277-baa3-7daa3f209e76:urn:uuid:def3ba91-799f-4347-833e-d9a97d3359dc"
</code></pre>
A weak eTag for a feed with only one ATOM entry looks like this:
<pre><code>
W/"urn:uuid:21d39ce9-940b-4277-baa3-7daa3f209e76:urn:uuid:21d39ce9-940b-4277-baa3-7daa3f209e76"
</code></pre>

###Web.xml Configuration###

This section pertains to you only if Atom Hopper will NOT be the only WAR managed by Apache Tomcat 6 or 7. If Atom Hopper is the only WAR then you do not need to do anything.

Assuming Atom Hopper needs to have it's own url-pattern mapping you will also need to set the atomhopper-url-pattern value to ensure your ATOM entry self links are correct. Below is an example of using "atom" as a url-pattern and corresponding setting for atomhopper-url-pattern.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">
    <display-name>Atom Hopper Server</display-name>
    <description>ATOM</description>

<!-- stuff omitted for brevity -->

        <!--
            Use the atomhopper-url-pattern to match the url-pattern so ATOM entries
            use valid self URLs.

            Ex: <url-pattern>/atom/*</url-pattern>

            Map the above as:
            <param-name>atomhopper-url-pattern</param-name>
            <param-value></atom/param-value>

            Note: There is not need for the asterix on the
            atomhopper-url-pattern param-value

        -->
        <init-param>
            <param-name>atomhopper-url-pattern</param-name>
            <param-value>/atom/</param-value>
        </init-param>
    </servlet>

    <servlet-mapping id="atom-hopper-mapping">
        <servlet-name>Atom-Hopper</servlet-name>
        <url-pattern>/atom/*</url-pattern>
    </servlet-mapping>
</web-app>
</pre>
```

###Notes Regarding licensing###

*All files contained with this distribution of Atom Hopper are licenced 
under the Apache License v2.0 (http://www.apache.org/licenses/LICENSE-2.0).
You must agree to the terms of this license and abide by them before
viewing, utilizing or distributing the source code contained within this distribution.*
