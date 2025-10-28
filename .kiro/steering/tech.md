# Technology Stack

## Build System
- **Maven 3.x**: Multi-module project structure
- **Java 8**: Source and target compatibility
- **Jenkins**: CI/CD pipeline with GitHub integration

## Core Technologies
- **Apache Abdera 1.1.2**: Atom Publishing framework
- **Spring Framework 5.2.25**: Dependency injection and JDBC support
- **Eclipse Jetty 9.4.17**: Embedded server and servlet container
- **AspectJ 1.6.9**: Aspect-oriented programming support

## Database Support
- **PostgreSQL**: Primary production database
- **H2 2.1.210**: Development and testing
- **MySQL 8.0.16**: Production alternative
- **MongoDB**: NoSQL option (maintenance mode)
- **Hibernate 4.1.3**: ORM support (maintenance mode)

## Testing & Quality
- **JUnit 4.13.1**: Unit testing framework
- **Mockito 1.8.5**: Mocking framework
- **JMeter**: Performance and regression testing
- **Yammer Metrics**: Performance monitoring

## Logging & Monitoring
- **Logback 1.2.x**: Primary logging framework
- **SLF4J 1.6.5**: Logging abstraction
- **Codahale Metrics 3.0.1**: Application metrics
- **Graphite integration**: Metrics reporting

## Common Commands

### Build and Test
```bash
# Full build with tests
mvn clean install

# Run tests only
mvn test

# Initialize and test (Jenkins pipeline)
mvn initialize test

# Skip tests during build
mvn clean install -DskipTests
```

### Development
```bash
# Run with embedded Jetty
java -jar server/target/atomhopper-server-*.jar

# Package WAR for deployment
mvn clean package
```

### Docker
```bash
# Build Docker image
docker build -t atomhopper:latest-alpine .

# Run with H2 (default)
docker run -d --name atomhopper -p 8080:8080 atomhopper:latest-alpine

# Run with PostgreSQL
docker run -d --name atomhopper -p 8080:8080 \
  -e DB_TYPE=PostgreSQL \
  -e DB_USER=postgres \
  -e DB_PASSWORD=password \
  -e DB_HOST=localhost:5432 \
  atomhopper:latest-alpine
```