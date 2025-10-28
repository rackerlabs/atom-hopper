# Project Structure

## Root Level Organization
```
atom-hopper/
├── hopper/              # Core framework and interfaces
├── server/              # Standalone server implementation
├── atomhopper/          # WAR packaging module
├── adapters/            # Data adapter implementations
├── test-suite/          # Integration tests
├── test-util/           # Testing utilities
├── tests/               # Performance and regression tests
├── docker/              # Docker configuration
└── documentation/       # Project documentation
```

## Core Modules

### hopper/ - Core Framework
- **Purpose**: Core interfaces, abstractions, and framework code
- **Key packages**:
  - `org.atomhopper.adapter`: Core adapter interfaces (FeedPublisher, FeedSource)
  - `org.atomhopper.abdera`: Abdera integration and filters
  - `org.atomhopper.config`: Configuration processing
  - `org.atomhopper.util`: Utilities and helper classes

### server/ - Standalone Server
- **Purpose**: Embedded Jetty server for standalone deployment
- **Key classes**: AtomHopperServer, AtomHopperJettyServerBuilder

### atomhopper/ - WAR Package
- **Purpose**: Web application packaging for servlet containers
- **Contains**: Web.xml, context configuration, deployment descriptors

## Adapter Architecture

### adapters/ - Data Adapters
Each adapter follows the same structure pattern:
```
adapters/{adapter-name}/
├── src/main/java/org/atomhopper/{adapter}/
│   ├── adapter/         # FeedPublisher/FeedSource implementations
│   ├── model/           # Data models and entities
│   └── query/           # Query builders and utilities
├── src/main/resources/
│   └── ddl/             # Database schema files
└── src/test/java/       # Unit tests
```

**Active Adapters**:
- `jdbc/`: Primary JDBC adapter (PostgreSQL focus)
- `migration/`: Data migration utilities

**Maintenance Mode**:
- `hibernate/`: Hibernate ORM adapter
- `mongodb/`: MongoDB NoSQL adapter
- `postgres-adapter/`: Legacy PostgreSQL adapter
- `dynamoDB_adapters/`: AWS DynamoDB adapter

## Configuration Structure

### Configuration Files
- `META-INF/atom-server.cfg.xml`: Main server configuration
- `META-INF/application-context.xml`: Spring context configuration
- `logback.xml`: Logging configuration
- `atom-hopper-config.xsd`: Configuration schema

### Package Conventions
- **Interfaces**: `org.atomhopper.adapter.*`
- **Implementations**: `org.atomhopper.{technology}.adapter.*`
- **Models**: `org.atomhopper.{technology}.model.*`
- **Queries**: `org.atomhopper.{technology}.query.*`
- **Tests**: Mirror main package structure in test directories

## Testing Structure

### test-suite/
- Integration tests using embedded Jetty
- End-to-end feed operations testing
- HTTP client-based test harness

### tests/
- `regression/`: JMeter-based regression tests
- `release/`: Release validation tests
- `customjdbc-regression/`: JDBC-specific regression tests

## Build Artifacts
- **JAR**: `server/target/atomhopper-server-*.jar` (standalone)
- **WAR**: `atomhopper/target/atomhopper-*.war` (servlet container)
- **Docker**: Multi-stage build with Tomcat base image