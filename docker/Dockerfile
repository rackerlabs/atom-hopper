#base tomcat 9 with openjdk 8
FROM tomcat:9.0-jdk8 as tomcat

# Build stage
FROM maven:3.8-openjdk-8-slim as builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:8-jre

LABEL maintainer="AtomHopperTeam@rackspace.com" \
      #Atom Hopper version
      version="1.2.48" \
      description="Docker image for Atom Hopper"

    #The database type
ENV DB_TYPE=H2 \
    #Database username
    DB_USER=sa \
    #Database password
    DB_PASSWORD= \
    #Database Host:Port
    DB_HOST=h2 \
    AH_VERSION=1.2.48 \
    CATALINA_HOME=/opt/tomcat \
    AH_HOME=/opt/atomhopper  \
    PATH=${PATH}:${CATALINA_HOME}/bin:${AH_HOME}

RUN mkdir -p "${CATALINA_HOME}" "${AH_HOME}" /etc/atomhopper/ /var/log/atomhopper/ "${CATALINA_HOME}/webapps"

WORKDIR ${AH_HOME}

COPY --from=tomcat /usr/local/tomcat ${CATALINA_HOME}
COPY docker/start.sh .
COPY --from=builder /app/atomhopper/target/atomhopper-${AH_VERSION}.war ${CATALINA_HOME}/webapps/atomhopper.war
COPY --from=builder /app/atomhopper/src/main/webapp/META-INF/application-context.xml /etc/atomhopper/
COPY --from=builder /app/atomhopper/src/main/resources/META-INF/atom-server.cfg.xml /etc/atomhopper/
COPY --from=builder /app/atomhopper/src/main/webapp/META-INF/template-logback.xml /etc/atomhopper/logback.xml

RUN apt-get update && apt-get install -y curl \
    && chmod +x ${AH_HOME}/start.sh \
    && rm -rf ${CATALINA_HOME}/webapps/ROOT \
    && apt-get clean && rm -rf /var/lib/apt/lists/* 

EXPOSE 8080

# Health check to verify the application is running
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/atomhopper/buildinfo || exit 1

CMD ["/opt/atomhopper/start.sh"]