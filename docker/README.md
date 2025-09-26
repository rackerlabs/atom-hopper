# docker build image and run the conatiner
Your current direcotry should be pointing to ***atom-hopper/docker***. 
Run the following command to build an image.
```
$docker build -t atomhopper:latest-alpine .
```
You can use the following command to run a container by providing the appropriate values to the variables.
```
$docker run -d --name [Container_Name] -p 8080:8080 -e DB_TYPE=[Database_Type (PostgreSQL, MySQL)] -e DB_USER=[Database_Username] -e DB_PASSWORD=[Database_Password] -e DB_HOST=[IP:PORT] -e AH_DOMAIN=[Domain:Port] -e AH_SCHEME=[http|https] atomhopper:latest-alpine
```

To run atomhopper with default database configuration (H2) and port 8080
```
$docker run -d --name atomhopper -p 8080:8080 atomhopper:latest-alpine
```
Test the sample feed at http://localhost:8080/namespace/feed

H2 is the default databse configured to be used. The databse file for this is present under */opt/atomhopper*

Following environment variables are set by default 
```
JAVA_HOME "/opt/java/openjdk8/jre"
CATALINA_HOME "/opt/tomcat"
AH_HOME "/opt/atomhopper"
AH_VERSION "1.2.33"
AH_DOMAIN "localhost:8080"
AH_SCHEME "http"
```

## Environment Variables

### Database Configuration
For specific database configuration of your choice (PostgreSQL, MySQL) provide values for the variables:
- `DB_TYPE`: Database type (H2, PostgreSQL, MySQL)
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password  
- `DB_HOST`: Database host and port (e.g., "10.0.0.1:5432")

### Domain Configuration
For dual domain support (internal vs external):
- `AH_INTERNAL_DOMAIN`: Internal domain for internal API calls (e.g., "internal.cloudfeeds.local:8080")
- `AH_EXTERNAL_DOMAIN`: External domain for public API responses (e.g., "feeds.example.com")
- `AH_INTERNAL_SCHEME`: Internal URL scheme ("http" or "https")
- `AH_EXTERNAL_SCHEME`: External URL scheme ("http" or "https")
- `AH_DOMAIN_MODE`: Domain selection mode ("internal", "external", or "request-based")

## Examples

### Build Requirements
Before building the Docker image, ensure the WAR file is built:
```bash
# Build the project first
mvn clean package

# Then build the Docker image
cd docker
docker build -t atomhopper:latest .
```

### PostgreSQL with Dual Domain Support
```bash
$docker run -d --name atomhopper -p 8080:8080 \
  -e DB_TYPE=PostgreSQL \
  -e DB_USER=postgresql \
  -e DB_PASSWORD=postgresql \
  -e DB_HOST=10.0.0.1:5432 \
  -e AH_INTERNAL_DOMAIN=internal.cloudfeeds.local:8080 \
  -e AH_EXTERNAL_DOMAIN=feeds.example.com \
  -e AH_INTERNAL_SCHEME=http \
  -e AH_EXTERNAL_SCHEME=https \
  -e AH_DOMAIN_MODE=external \
  atomhopper:latest
```

### Internal-Only Configuration
```bash
$docker run -d --name atomhopper-internal -p 8080:8080 \
  -e AH_INTERNAL_DOMAIN=internal.cloudfeeds.local:8080 \
  -e AH_EXTERNAL_DOMAIN=internal.cloudfeeds.local:8080 \
  -e AH_INTERNAL_SCHEME=http \
  -e AH_EXTERNAL_SCHEME=http \
  -e AH_DOMAIN_MODE=internal \
  atomhopper:latest
```

### External-Only Configuration  
```bash
$docker run -d --name atomhopper-external -p 8080:8080 \
  -e AH_INTERNAL_DOMAIN=feeds.example.com \
  -e AH_EXTERNAL_DOMAIN=feeds.example.com \
  -e AH_INTERNAL_SCHEME=https \
  -e AH_EXTERNAL_SCHEME=https \
  -e AH_DOMAIN_MODE=external \
  atomhopper:latest
```

### Request-Based Domain Switching (Future Enhancement)
```bash
$docker run -d --name atomhopper-smart -p 8080:8080 \
  -e AH_INTERNAL_DOMAIN=internal.cloudfeeds.local:8080 \
  -e AH_EXTERNAL_DOMAIN=feeds.example.com \
  -e AH_INTERNAL_SCHEME=http \
  -e AH_EXTERNAL_SCHEME=https \
  -e AH_DOMAIN_MODE=request-based \
  atomhopper:latest
```



