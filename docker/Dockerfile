#base tomcat 9 with openjdk 8
FROM tomcat:9.0.41-jdk8 as tomcat

FROM adoptopenjdk/openjdk8:alpine-slim

# Build arguments for GitHub authentication
ARG GITHUB_TOKEN
ARG GITHUB_ACTOR

# Validate that authentication arguments are provided
RUN if [ -z "$GITHUB_TOKEN" ] || [ -z "$GITHUB_ACTOR" ]; then \
        echo "ERROR: GITHUB_TOKEN and GITHUB_ACTOR build arguments are required for GitHub Packages authentication"; \
        exit 1; \
    fi

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
    AH_VERSION=1.2.33 \
    CATALINA_HOME=/opt/tomcat \
    AH_HOME=/opt/atomhopper  \
    PATH=${PATH}:${CATALINA_HOME}/bin:${AH_HOME}

RUN mkdir -p "${CATALINA_HOME}" "${AH_HOME}" /etc/atomhopper/ /var/log/atomhopper/

WORKDIR ${AH_HOME}

COPY --from=tomcat /usr/local/tomcat ${CATALINA_HOME}
COPY docker/start.sh .

RUN apk --no-cache add curl unzip \
    && curl -L -u ${GITHUB_ACTOR}:${GITHUB_TOKEN} -o atomhopper.war \
       "https://maven.pkg.github.com/rackerlabs/atom-hopper/org/atomhopper/atomhopper/${AH_VERSION}/atomhopper-${AH_VERSION}.war" \
    && unzip atomhopper.war META-INF/application-context.xml META-INF/template-logback.xml WEB-INF/classes/META-INF/atom-server.cfg.xml -d . \
    && mv META-INF/application-context.xml WEB-INF/classes/META-INF/atom-server.cfg.xml /etc/atomhopper/ \
    && mv META-INF/template-logback.xml /etc/atomhopper/logback.xml \
    && mv atomhopper.war ${CATALINA_HOME}/webapps/ROOT.war \
    && rm -rf META-INF WEB-INF \
    && chmod +x ${AH_HOME}/start.sh

EXPOSE 8080

CMD ["/opt/atomhopper/start.sh"]