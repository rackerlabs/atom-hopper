package org.atomhopper.util;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.jmx.ConnectionPool;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.*;

/**
 * This class takes a map of org.apache.tomcat.jdbc.pool.DataSource beans, and the contact information to a graphite
 * server.  The bean will register the MBeans with the JVM, the MetricRegistry & send the data to the graphite server.
 *
 * .count is added to each attribute name to force graphite to report the actual value as opposed to an average.
 *
 * @param map  Map<String, DataSource> - The key is the OName to register the MBean under (e.g.,
 *             ConnectionPools:name=demo-jdbc-new-feed-read-repository-bean) and the corresponding DataSource object
 * @param host String - the graphite server hostname
 * @param port int - the graphite server port
 * @param period int - the period in seconds between data reported to the graphite server
 * @param prefix String - the prefix for the MBeans in the graphite server
 *
 */
public class TomcatJdbcPoolGraphiteReporter {

    public TomcatJdbcPoolGraphiteReporter( Map<String, DataSource> map,
                                           String host,
                                           int port,
                                           int period,
                                           String prefix ) throws
          MalformedObjectNameException,
          SQLException,
          NotCompliantMBeanException,
          InstanceAlreadyExistsException,
          MBeanRegistrationException, InstanceNotFoundException {

        synchronized ( this ) {

            MetricRegistry registry = new MetricRegistry();

            MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            // register with Metrics
            for( String name : map.keySet() ) {

                ObjectName oName = new ObjectName( name );

                if( server.isRegistered( oName ) ) {

                    server.unregisterMBean( oName );
                }

                ConnectionPool pool = map.get( name ).createPool().getJmxPool();

                server.registerMBean( pool, oName );

                registry.register( name( oName.getKeyProperty( "name" ), "NumActive.count" ),
                                   new JmxAttributeGauge( oName, "NumActive" ) );

                registry.register( name( oName.getKeyProperty( "name" ), "NumIdle.count" ),
                                   new JmxAttributeGauge( oName, "NumIdle" ) );

                registry.register( name( oName.getKeyProperty( "name" ), "Size.count" ),
                                   new JmxAttributeGauge( oName, "Size" ) );
            }

            Graphite graphite = new Graphite( new InetSocketAddress( host, port) );
            final GraphiteReporter reporter = GraphiteReporter.forRegistry( registry )
                  .prefixedWith( prefix )
                  .convertRatesTo( TimeUnit.SECONDS )
                  .convertDurationsTo( TimeUnit.MILLISECONDS )
                  .filter( MetricFilter.ALL )
                  .build( graphite );
            reporter.start( period, TimeUnit.SECONDS );

            Runtime.getRuntime().addShutdownHook( new Thread() {

                public void run()  {

                    reporter.stop();
                }
            });
/*
            Helpful for testing

            ConsoleReporter console = ConsoleReporter.forRegistry( registry )
                  .convertRatesTo(TimeUnit.SECONDS)
                  .convertDurationsTo( TimeUnit.MILLISECONDS )
                  .build();
            console.start( period, TimeUnit.SECONDS );
*/
        }
    }
}
