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
For dynamic domain configuration (internal vs external):
- `AH_DOMAIN`: Domain and port for link generation (e.g., "internal.example.com:8080", "api.example.com")
- `AH_SCHEME`: URL scheme for link generation ("http" or "https")

## Examples

### PostgreSQL with External Domain
```bash
$docker run -d --name atomhopper -p 8080:8080 \
  -e DB_TYPE=PostgreSQL \
  -e DB_USER=postgresql \
  -e DB_PASSWORD=postgresql \
  -e DB_HOST=10.0.0.1:5432 \
  -e AH_DOMAIN=api.example.com \
  -e AH_SCHEME=https \
  atomhopper:latest-alpine
```

### Internal Domain Configuration
```bash
$docker run -d --name atomhopper -p 8080:8080 \
  -e AH_DOMAIN=internal.cloudfeeds.local:8080 \
  -e AH_SCHEME=http \
  atomhopper:latest-alpine
```

### External Domain Configuration  
```bash
$docker run -d --name atomhopper -p 8080:8080 \
  -e AH_DOMAIN=feeds.example.com \
  -e AH_SCHEME=https \
  atomhopper:latest-alpine
```



