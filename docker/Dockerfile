#base tomcat 9 with openjdk 8
FROM tomcat:9.0.41-jdk8 as tomcat

FROM adoptopenjdk/openjdk8:jdk8u275-b01-alpine-slim

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
COPY start.sh .

RUN apk --no-cache add curl \
    && curl -o atomhopper.war https://maven.research.rackspacecloud.com/content/repositories/releases/org/atomhopper/atomhopper/${AH_VERSION}/atomhopper-${AH_VERSION}.war \
    && unzip atomhopper.war META-INF/application-context.xml META-INF/template-logback.xml WEB-INF/classes/META-INF/atom-server.cfg.xml -d . \
    && mv META-INF/application-context.xml WEB-INF/classes/META-INF/atom-server.cfg.xml /etc/atomhopper/ \
    && mv META-INF/template-logback.xml /etc/atomhopper/logback.xml \
    && mv atomhopper.war ${CATALINA_HOME}/webapps/ROOT.war \
    && rm -rf META-INF WEB-INF \
    && chmod +x ${AH_HOME}/start.sh 

EXPOSE 8080

CMD ["start.sh"]

