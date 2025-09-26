#base tomcat 9 with openjdk 8
FROM tomcat:9.0-jdk8 as tomcat

FROM openjdk:8-jre-alpine

LABEL maintainer="AtomHopperTeam@rackspace.com" \
      #Atom Hopper version
      version="1.2.33" \
      description="Docker image for Atom Hopper"

    #The database type
ENV DB_TYPE=H2 \
    #Database username
    DB_USER=sa \
    #Database password
    DB_PASSWORD= \
    #Database Host:Port
    DB_HOST=h2 \
    #Internal domain for internal API calls
    AH_INTERNAL_DOMAIN=localhost:8080 \
    #External domain for public API responses
    AH_EXTERNAL_DOMAIN=localhost:8080 \
    #Internal scheme (http or https)
    AH_INTERNAL_SCHEME=http \
    #External scheme (http or https)
    AH_EXTERNAL_SCHEME=http \
    #Domain selection mode: internal, external, or request-based
    AH_DOMAIN_MODE=external \
    AH_VERSION=1.2.35-SNAPSHOT \
    CATALINA_HOME=/opt/tomcat \
    AH_HOME=/opt/atomhopper  \
    PATH=${PATH}:${CATALINA_HOME}/bin:${AH_HOME}

RUN mkdir -p "${CATALINA_HOME}" "${AH_HOME}" /etc/atomhopper/ /var/log/atomhopper/ 

WORKDIR ${AH_HOME}

COPY --from=tomcat /usr/local/tomcat ${CATALINA_HOME}
COPY docker/start.sh .

# Copy the locally built WAR file instead of downloading
# Note: Build the project first with 'mvn clean package' from the root directory
COPY atomhopper/target/atomhopper-*.war atomhopper.war

RUN apk --no-cache add curl \
    && unzip atomhopper.war META-INF/application-context.xml META-INF/template-logback.xml WEB-INF/classes/META-INF/atom-server.cfg.xml -d . \
    && mv META-INF/application-context.xml WEB-INF/classes/META-INF/atom-server.cfg.xml /etc/atomhopper/ \
    && mv META-INF/template-logback.xml /etc/atomhopper/logback.xml \
    && mv atomhopper.war ${CATALINA_HOME}/webapps/ROOT.war \
    && rm -rf META-INF WEB-INF \
    && chmod +x ${AH_HOME}/start.sh 

EXPOSE 8080

CMD ["/opt/atomhopper/start.sh"]