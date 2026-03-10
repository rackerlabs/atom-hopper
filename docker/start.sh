#!/bin/bash
set -e

export APP_CTX_PATH=/etc/atomhopper
echo "Using APP_CTX_PATH=$APP_CTX_PATH"
echo "Database type selected: $DB_TYPE"

# Verify required files exist
echo "Verifying required files..."
if [ ! -f "$APP_CTX_PATH/application-context.xml" ]; then
    echo "ERROR: application-context.xml not found at $APP_CTX_PATH"
    exit 1
fi

if [ ! -f "$APP_CTX_PATH/atom-server.cfg.xml" ]; then
    echo "ERROR: atom-server.cfg.xml not found at $APP_CTX_PATH"
    exit 1
fi

if [ ! -f "$CATALINA_HOME/webapps/atomhopper.war" ]; then
    echo "ERROR: AtomHopper WAR file not found at $CATALINA_HOME/webapps/atomhopper.war"
    exit 1
fi

echo "WAR file found: $(ls -la $CATALINA_HOME/webapps/atomhopper.war)"

# For PostgreSQL, we don't need to modify the application-context.xml since it uses environment variables
echo "Using PostgreSQL configuration with:"
echo "  DB_HOST: $DB_HOST"
echo "  DB_USER: $DB_USER"
echo "  Database: atomhopper"

# Wait for PostgreSQL to be ready (if DB_HOST is not localhost)
if [ "$DB_HOST" != "localhost:5432" ] && [ "$DB_HOST" != "localhost" ]; then
    echo "Waiting for PostgreSQL at $DB_HOST to be ready..."
    # Extract host and port
    DB_HOST_ONLY=$(echo $DB_HOST | cut -d: -f1)
    DB_PORT=$(echo $DB_HOST | cut -d: -f2)
    
    # Simple connection test
    for i in {1..30}; do
        if nc -z $DB_HOST_ONLY $DB_PORT 2>/dev/null; then
            echo "PostgreSQL is ready!"
            break
        fi
        echo "Waiting for PostgreSQL... ($i/30)"
        sleep 2
    done
fi

# Ensure Tomcat can write to webapps directory
chmod 755 $CATALINA_HOME/webapps
chmod 644 $CATALINA_HOME/webapps/atomhopper.war

echo "Starting Tomcat server..."
echo "AtomHopper will be available at: http://localhost:8080/atomhopper"
echo "Health check endpoint: http://localhost:8080/atomhopper/buildinfo"

# Start Tomcat server
exec $CATALINA_HOME/bin/catalina.sh run