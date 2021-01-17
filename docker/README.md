# docker build image and run the conatiner
Your current direcotry should be pointing to ***atom-hopper/docker***. 
Run the following command to build an image.
```
$docker build -t atomhopper:latest-alpine .
```
You can use the following command to run a container by provinding the appropriate values to the variables.
```
$docker run -d --name [Conatiner_Name] -p 8080:8080 -e DB_TYPE=[Database_Type (PostgreSQL, MySQL)] -e DB_USER=[Database_Username] -e DB_PASSWORD=[Database_Password] -e DB_HOST=[IP:PORT] atomhopper:latest-alpine
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
```
For specific databse configuration of your choice (PostgreSQL,MySQL) provide values for the variables DB_TYPE, DB_USER, DB_PASSWORD and DB_HOST

Example of running with a PostgreSQL databse hosted externally. 
```
$docker run -d --name atomhopper -p 8080:8080 -e DB_TYPE=PostgreSQL -e DB_USER=postgresql -e DB_PASSWORD=postgresql -e DB_HOST=10.0.0.1:5432 atomhopper:latest-alpine
```



