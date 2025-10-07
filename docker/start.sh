#!/bin/sh
export APP_CTX_PATH=/etc/atomhopper
echo "using APP_CTX_PATH="$APP_CTX_PATH

if [[ -e $APP_CTX_PATH/application-context.xml.orig ]]
then
    echo "Replacing application-context.xml with original config."
    mv $APP_CTX_PATH/application-context.xml.orig $APP_CTX_PATH/application-context.xml
fi

# Restore original atom-server.cfg.xml if it exists
if [[ -e $APP_CTX_PATH/atom-server.cfg.xml.orig ]]
then
    echo "Replacing atom-server.cfg.xml with original config."
    mv $APP_CTX_PATH/atom-server.cfg.xml.orig $APP_CTX_PATH/atom-server.cfg.xml
fi

echo "Database type selected:"$DB_TYPE
echo "External domain configured:"$AH_EXTERNAL_DOMAIN
echo "External scheme configured:"$AH_EXTERNAL_SCHEME
echo "Domain mode:"$AH_DOMAIN_MODE

#DB configuration
if [[ $DB_TYPE != 'H2' ]] ; then
    #Comment default H2 Database and backup the original file
    sed -i.orig -e '/Start H2 Config/a <!--' -e '/End H2 Config/i -->' $APP_CTX_PATH/application-context.xml
    
    #Enable databse based on the DB_TYPE value
    sed -i "/Start $DB_TYPE Config/{n;;d}" $APP_CTX_PATH/application-context.xml && sed -i "/Start $DB_TYPE Config/{n;n;n;n;n;n;;d}" $APP_CTX_PATH/application-context.xml
    
    #Remove databse username and password lines
    sed -i "/Start $DB_TYPE Config/{n;n;n;n;N;;d}" $APP_CTX_PATH/application-context.xml

    #Replace username and passowrd lines with env variable value
    sed -i -e "/End $DB_TYPE Config/i <entry key=\"hibernate.connection.username\" value=\"${DB_USER}\" \/>" -e "/End ${DB_TYPE} Config/i <entry key=\"hibernate.connection.password\" value=\"${DB_PASSWORD}\" \/>" $APP_CTX_PATH/application-context.xml

    #DB_HOST configuration
    if [ "$DB_TYPE" = 'MySQL' ] ; then
        sed -i -e "s/:mysql:\/\/localhost:8889/:mysql:\/\/$DB_HOST/g" $APP_CTX_PATH/application-context.xml
    fi
    if [ "$DB_TYPE" = 'PostgreSQL' ] ; then
        sed -i -e "s/:postgresql:\/\/localhost:5432/:postgresql:\/\/$DB_HOST/g" $APP_CTX_PATH/application-context.xml
    fi
fi

# Domain and Scheme configuration
echo "Configuring domain and scheme..."

# Backup original atom-server.cfg.xml if not already backed up
if [[ ! -e $APP_CTX_PATH/atom-server.cfg.xml.orig ]]
then
    cp $APP_CTX_PATH/atom-server.cfg.xml $APP_CTX_PATH/atom-server.cfg.xml.orig
fi

# For request-based mode, we use external as fallback in config
# The actual domain resolution happens at runtime based on request headers
SELECTED_DOMAIN="$AH_EXTERNAL_DOMAIN"
SELECTED_SCHEME="$AH_EXTERNAL_SCHEME"

if [ "$AH_DOMAIN_MODE" = "request-based" ]; then
    echo "Using request-based domain configuration"
    echo "Fallback domain: $SELECTED_DOMAIN, fallback scheme: $SELECTED_SCHEME"
    echo "Actual domains will be determined from incoming request Host headers"
else
    echo "Using static external domain configuration"
    echo "Domain: $SELECTED_DOMAIN, scheme: $SELECTED_SCHEME"
fi

# Replace domain and scheme in atom-server.cfg.xml (used as fallback)
sed -i "s/domain=\"[^\"]*\"/domain=\"${SELECTED_DOMAIN}\"/g" $APP_CTX_PATH/atom-server.cfg.xml
sed -i "s/scheme=\"[^\"]*\"/scheme=\"${SELECTED_SCHEME}\"/g" $APP_CTX_PATH/atom-server.cfg.xml

echo "Domain configuration completed."

#Start tomcat server
sh /opt/tomcat/bin/catalina.sh run