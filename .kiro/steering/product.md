# Product Overview

Atom Hopper is a Java-based ATOMPub server framework for accessing, processing, aggregating and indexing Atom formatted events. It provides a scalable, stateless solution for handling Atom XML data based on the Atom Syndication Format and Atom Publishing Protocol.

## Key Features

- **Multi-database support**: H2, PostgreSQL, MySQL, MongoDB
- **Flexible deployment**: Standalone JAR with embedded Jetty or WAR file for servlet containers
- **Scalable architecture**: Stateless design allows horizontal scaling
- **Adapter pattern**: Pluggable data adapters for different storage backends
- **High performance**: Designed for high-load scenarios with metrics support

## Current Adapter Status

- **JDBC Adapter**: Active development (primary focus)
- **Migration Adapter**: Active development
- **Hibernate Adapter**: Maintenance mode only
- **MongoDB Adapter**: Maintenance mode only
- **Postgres Adapter**: Maintenance mode only

## Primary Use Cases

- Event feed aggregation and distribution
- Atom feed publishing and consumption
- Event streaming and archival
- Integration with OpenStack and Rackspace infrastructure