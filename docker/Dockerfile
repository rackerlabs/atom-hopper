# Multi-stage build: first stage for building the application
FROM openjdk:8-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache curl tar bash \
    && mkdir -p /usr/share/maven /usr/share/maven/ref \
    && curl -fsSL -o /tmp/apache-maven.tar.gz https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Set Maven environment
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Copy source code
WORKDIR /app
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Second stage: runtime image
FROM openjdk:8-jre-alpine

LABEL maintainer="AtomHopperTeam@rackspace.com" \
      #Atom Hopper version
      version="1.2.33" \
      description="Docker image for Atom Hopper"

    #The database type
ENV DB_TYPE=PostgreSQL \
    #Database driver class
    DB_DRIVER=org.postgresql.Driver \
    #Database connection URL
    DB_URL=jdbc:postgresql://localhost:5432/atomhopper \
    #Database username
    DB_USER=postgres \
    #Database password
    DB_PASSWORD=password \
    #Database Host:Port (legacy, kept for compatibility)
    DB_HOST=localhost:5432 \
    #External domain for public API responses (fallback when request-based fails)
    AH_EXTERNAL_DOMAIN=localhost:8080 \
    #External scheme (http or https) (fallback when request-based fails)
    AH_EXTERNAL_SCHEME=http \
    #Domain selection mode: external or request-based
    AH_DOMAIN_MODE=request-based \
    AH_VERSION=1.2.35-SNAPSHOT \
    CATALINA_HOME=/opt/tomcat \
    AH_HOME=/opt/atomhopper  \
    PATH=${PATH}:${CATALINA_HOME}/bin:${AH_HOME}

RUN mkdir -p "${CATALINA_HOME}" "${AH_HOME}" /etc/atomhopper/ /var/log/atomhopper/ 

WORKDIR ${AH_HOME}

# Download and install Tomcat
RUN apk --no-cache add curl wget \
    && wget -O /tmp/tomcat.tar.gz https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.65/bin/apache-tomcat-9.0.65.tar.gz \
    && tar -xzf /tmp/tomcat.tar.gz -C /tmp \
    && rm -rf ${CATALINA_HOME} \
    && mv /tmp/apache-tomcat-9.0.65 ${CATALINA_HOME} \
    && rm /tmp/tomcat.tar.gz
COPY docker/start.sh .

# Copy the WAR file from the builder stage
COPY --from=builder /app/atomhopper/target/atomhopper-*.war atomhopper.war

RUN rm -rf ${CATALINA_HOME}/webapps/* \
    && mkdir -p ${CATALINA_HOME}/webapps \
    && cp atomhopper.war ${CATALINA_HOME}/webapps/ROOT.war \
    && unzip atomhopper.war META-INF/application-context.xml META-INF/template-logback.xml WEB-INF/classes/META-INF/atom-server.cfg.xml -d . \
    && mv META-INF/application-context.xml WEB-INF/classes/META-INF/atom-server.cfg.xml /etc/atomhopper/ \
    && mv META-INF/template-logback.xml /etc/atomhopper/logback.xml \
    && rm -rf META-INF WEB-INF atomhopper.war \
    && chmod +x ${AH_HOME}/start.sh 

EXPOSE 8080

CMD ["/opt/atomhopper/start.sh"]