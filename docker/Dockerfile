#base tomcat 9 with openjdk 8
FROM tomcat:9.0.41-jdk8 as tomcat

# Build stage
FROM maven:3.8.4-openjdk-8-slim as builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM adoptopenjdk/openjdk8:alpine-slim

LABEL maintainer="AtomHopperTeam@rackspace.com" \
      #Atom Hopper version
      version="1.2.46" \
      description="Docker image for Atom Hopper"

    #The database type
ENV DB_TYPE=H2 \
    #Database username
    DB_USER=sa \
    #Database password
    DB_PASSWORD= \
    #Database Host:Port
    DB_HOST=h2 \
    AH_VERSION=1.2.46 \
    CATALINA_HOME=/opt/tomcat \
    AH_HOME=/opt/atomhopper  \
    PATH=${PATH}:${CATALINA_HOME}/bin:${AH_HOME}

RUN mkdir -p "${CATALINA_HOME}" "${AH_HOME}" /etc/atomhopper/ /var/log/atomhopper/ 

WORKDIR ${AH_HOME}

COPY --from=tomcat /usr/local/tomcat ${CATALINA_HOME}
COPY docker/start.sh .
COPY --from=builder /app/atomhopper/target/atomhopper-${AH_VERSION}.war ${CATALINA_HOME}/webapps/ROOT.war
COPY --from=builder /app/atomhopper/src/main/webapp/META-INF/application-context.xml /etc/atomhopper/
COPY --from=builder /app/atomhopper/src/main/resources/META-INF/atom-server.cfg.xml /etc/atomhopper/
COPY --from=builder /app/atomhopper/src/main/webapp/META-INF/template-logback.xml /etc/atomhopper/logback.xml

RUN apk --no-cache add curl \
    && chmod +x ${AH_HOME}/start.sh 

EXPOSE 8080

CMD ["/opt/atomhopper/start.sh"]