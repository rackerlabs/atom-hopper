<pre>
    ___   __                     __  __                            
   /   | / /_____  ____ ___     / / / /____  ____  ____  ___  _____
  / /| |/ __/ __ \/ __ `__ \   / /_/ // __ \/ __ \/ __ \/ _ \/ ___/
 / ___ / /_/ /_/ / / / / / /  / __  // /_/ / /_/ / /_/ /  __/ /    
/_/  |_\__/\____/_/ /_/ /_/  /_/ /_/ \____/ .___/ .___/\___/_/     
                                         /_/   /_/                 
</pre>

#ATOM Hopper - A Java ATOMPub Server#

[Atom Hopper](http://atomhopper.org) is a framework for accessing, processing, aggregating and indexing Atom formatted events. Atom Hopper was designed to make it easy to build both generalized and specialized persistence mechanisms for Atom XML data, based on the Atom Syndication Format and the Atom Publishing Protocol.

Benefits:

* Simple. Atom Hopper is easy to use. It can be used out-of-the-box as an executable JAR (running within an embedded Jetty Server). For more flexibility, it can be deployed as a WAR file into any Servlet container (ie: Tomcat, Jetty, etc.). Most applications can use Atom Hopper with minimal configuration to specify the Atom Workspaces and the Content storage.
* Scalable. Atom Hopper is very scalable because it is designed to be stateless, allowing state to be distributed across the web.
* Layered. Atom Hopper allows any number of intermediaries, such as proxies, gateways, and firewalls so one can easily layer aspects such as Security, Compression, etc. on an as needed basis.
* Built on a strong foundation. It is built on top of several open source projects such as [Apache Abdera](http://abdera.apache.org/) (a Java-based Atom Publishing framework), [Hibernate](http://www.hibernate.org/), and [MongoDB](http://www.mongodb.org/).
* Flexible. Atom Hopper currently supports the following relational databases: [H2](http://www.h2database.com/), [PostgresSQL](http://www.postgresql.org/), and [MySQL](http://www.mysql.com/) (plus others that work with Hibernate) as well as the NoSQL database [MongoDB](http://www.mongodb.org/).
* High performance. Atom Hopper can handle high loads with high accuracy.
* Improving. Atom Hopper is under development and actively being worked on.
* Atom Hopper is currently being used at [Rackspace](http://www.rackspace.com/) in conjunction with [OpenStack](http://openstack.org).

Atom Hopper works well with [Repose](http://openrepose.org/) especially if you need:
* Authentication and Authorization
* Rate Limiting
* Versioning
* HTTP Logging

###Notes Regarding Data Adapter###
The current status of the Atom Hopper Data Adapters is as follows:

* JDBC Data Adapter - ongoing development

* Hibernate Data Adapter - not currently adding features/fixing defects

* MongoDB Data Adapter - not currently adding features/fixing defects

* Migration Data Adapter - ongoing development

* Postgres Data Adapter - not currently adding features/fixing defects

## Installation and Deployment

### Quick Start with Docker

The easiest way to get Atom Hopper running is with Docker:

```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build and run manually
docker build -f docker/Dockerfile -t atomhopper .
docker run -p 8080:8080 atomhopper
```

Access the application at: http://localhost:8080/atomhopper

### Building from Source

```bash
# Build the project
mvn clean package

# Run with embedded Jetty (development)
java -jar hopper/target/hopper-*.jar

# Or deploy the WAR file to Tomcat
cp atomhopper/target/atomhopper-*.war /path/to/tomcat/webapps/
```

### Deployment Options

#### 1. GitHub Packages & Container Registry

This project is configured for automatic deployment to GitHub Packages and GitHub Container Registry via GitHub Actions:

- **Maven artifacts**: Published to GitHub Packages on every push to main/master
- **Docker images**: Published to `ghcr.io/rackerlabs/atom-hopper` 
- **WAR files**: Available as build artifacts

#### 2. Tomcat Deployment

**Automated Deployment:**
```bash
# Using the deployment script
./scripts/deploy-to-tomcat.sh [tomcat_host] [tomcat_port] [context_path]

# Example
./scripts/deploy-to-tomcat.sh localhost 8080 atomhopper
```

**Manual Deployment:**
1. Build the WAR file: `mvn clean package`
2. Copy `atomhopper/target/atomhopper-*.war` to your Tomcat `webapps` directory
3. Restart Tomcat or wait for auto-deployment

**Environment Variables for Deployment:**
- `TOMCAT_USER`: Tomcat manager username (default: admin)
- `TOMCAT_PASSWORD`: Tomcat manager password (default: admin123)

#### 3. Docker Deployment

**Using pre-built image:**
```bash
docker run -p 8080:8080 ghcr.io/rackerlabs/atom-hopper:latest
```

**With custom configuration:**
```bash
docker run -p 8080:8080 \
  -e DB_TYPE=H2 \
  -e DB_USER=sa \
  -e DB_PASSWORD= \
  -v /path/to/config:/etc/atomhopper \
  ghcr.io/rackerlabs/atom-hopper:latest
```

### Configuration

Atom Hopper can be configured using:
- Environment variables (for Docker deployments)
- Configuration files in `/etc/atomhopper/`
- System properties

Key configuration files:
- `atom-server.cfg.xml`: Main server configuration
- `application-context.xml`: Spring application context
- `logback.xml`: Logging configuration

### Health Check

Verify your deployment:
```bash
curl http://localhost:8080/atomhopper/buildinfo
```

For more detailed installation and configuration instructions, see the [Atom Hopper Wiki](https://github.com/rackerlabs/atom-hopper/wiki)

###Notes Regarding licensing###

*All files contained with this distribution of Atom Hopper are licenced 
under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
You must agree to the terms of this license and abide by them before
viewing, utilizing or distributing the source code contained within this distribution.*
