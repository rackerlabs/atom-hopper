# Use Tomcat base image directly
FROM tomcat:9.0-jdk8

LABEL maintainer="AtomHopperTeam@rackspace.com" \
      version="1.2.53" \
      description="Docker image for Atom Hopper"

# Environment variables
ENV AH_VERSION=1.2.53

# Copy pre-built WAR file to Tomcat webapps directory
COPY atomhopper/target/atomhopper-${AH_VERSION}.war ${CATALINA_HOME}/webapps/atomhopper.war

# Install curl for health checks
RUN apt-get update && apt-get install -y curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Verify WAR file was copied correctly
RUN ls -la ${CATALINA_HOME}/webapps/atomhopper.war && \
    echo "WAR file size: $(stat -c%s ${CATALINA_HOME}/webapps/atomhopper.war) bytes"

EXPOSE 8080

# Health check to verify the application is running
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/atomhopper/buildinfo || exit 1

# Use default Tomcat startup
CMD ["catalina.sh", "run"]