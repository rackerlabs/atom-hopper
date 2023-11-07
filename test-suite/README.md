<pre>
    ___   __                     __  __                            
   /   | / /_____  ____ ___     / / / /____  ____  ____  ___  _____
  / /| |/ __/ __ \/ __ `__ \   / /_/ // __ \/ __ \/ __ \/ _ \/ ___/
 / ___ / /_/ /_/ / / / / / /  / __  // /_/ / /_/ / /_/ /  __/ /    
/_/  |_\__/\____/_/ /_/ /_/  /_/ /_/ \____/ .___/ .___/\___/_/     
                                         /_/   /_/                 
</pre>

#How to switch between the different adapters like Hibernate adapter and JDBC adapter.#

The test-suite is designed to test the functionality of the atomhopper which uses different kinds of adapters to support different kind of DBs.
For running the functional test cases the adapter can be switched for 1 adapter to another and same functional test cases can be run with different adapters.
If in future a new DB is added then we can write the configuartion files for the new DB and try to run the same functional test cases. We should be able to insert and get the feeds from the new DB instance.



Steps:

* Rename atom-server.cfg.xml to atom-server_Hibernate.cfg.xml present at location /atom-hopper/test-suite/src/test/resources/META-INF/
* Rename atom-server_JDBC.cfg.xml to atom-server.cfg.xml present at location /atom-hopper/test-suite/src/test/resources/META-INF/
* Rename application-context.xml to application-context_Hibernate.xml present at location atom-hopper/test-suite/src/main/resources/META-INF
* Rename application-context_JDBC.xml to application-context.xml present at location atom-hopper/test-suite/src/main/resources/META-INF


###Notes Regarding Test Suite###

* All the existing test suite files should work with both the adapters.
* The working instance of postgresql is required for the JDBC adapter to work.
* Hibernate creates the internal database on Jetty server but postgres can't.
* The configuration related to JDBC(postgres db instance) can be changed in application-context_JDBC.xml file. The default configuration is already there in the file and pointing to local instance of postgres db running on 5432 port.
* Properties like allowOverrideDate, allowOverrideId can be added or their value can be changed in the file application-context_JDBC.xml.