<pre>
    ___   __                     __  __                            
   /   | / /_____  ____ ___     / / / /____  ____  ____  ___  _____
  / /| |/ __/ __ \/ __ `__ \   / /_/ // __ \/ __ \/ __ \/ _ \/ ___/
 / ___ / /_/ /_/ / / / / / /  / __  // /_/ / /_/ / /_/ /  __/ /    
/_/  |_\__/\____/_/ /_/ /_/  /_/ /_/ \____/ .___/ .___/\___/_/     
                                         /_/   /_/                 
</pre>

#ATOM Hopper - A Java ATOMPub Server#

Atom Hopper is a framework for accessing, processing, aggregating and indexing Atom formatted events. Atom Hopper was designed to make it easy to build both generalized and specialized persistence mechanisms for Atom XML data, based on the Atom Syndication Format and the Atom Publishing Protocol.

Benefits:

* Simple. Atom Hopper is easy to use. It can be used out-of-the-box as an executable JAR (running within an embedded Jetty Server). For more flexibility, it can be deployed as a WAR file into any Servlet container (ie: Tomcat, Jetty, etc.). Most applications can use Atom Hopper with minimal configuration to specify the Atom Workspaces and the Content storage.
* Scalable. Atom Hopper is very scalable because it is designed to be stateless, allowing state to be distributed across the web.
* Layered. Atom Hopper allows any number of intermediaries, such as proxies, gateways, and firewalls so one can easily layer aspects such as Security, Compression, etc. on an as needed basis.
* Built on a strong foundation. It is built on top of several open source projects such as [Apache Abdera](http://abdera.apache.org/) (a Java-based Atom Publishing framework), [Hibernate](http://www.hibernate.org/), and [MongoDB](http://www.mongodb.org/).
* Flexible. Atom Hopper currently supports the following relational databases: [H2](http://www.h2database.com/), [PostgresSQL](http://www.postgresql.org/), and [MySQL](http://www.mysql.com/) (plus others that work with Hibernate) as well as the NoSQL database [MongoDB](http://www.mongodb.org/).
* High performance. Atom Hopper can handle high loads with high accuracy.
* Improving. Atom Hopper is under development and actively being worked on.
* Atom Hopper is currently being used at [Rackspace Hosting](http://www.rackspace.com/).

To find out how to install and run Atom Hopper please see the [Atom Hopper Wiki](https://github.com/rackspace/atom-hopper/wiki)

###Notes Regarding licensing###

*All files contained with this distribution of Atom Hopper are licenced 
under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
You must agree to the terms of this license and abide by them before
viewing, utilizing or distributing the source code contained within this distribution.*