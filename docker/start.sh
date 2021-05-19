#!/bin/sh
export APP_CTX_PATH=/etc/atomhopper
echo "using APP_CTX_PATH="$APP_CTX_PATH

if [[ -e $APP_CTX_PATH/application-context.xml.orig ]]
then
    echo "Replacing application-context.xml with original config."
    mv $APP_CTX_PATH/application-context.xml.orig $APP_CTX_PATH/application-context.xml
fi
echo "Database type selected:"$DB_TYPE

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

#Start tomcat server
sh /opt/tomcat/bin/catalina.sh run