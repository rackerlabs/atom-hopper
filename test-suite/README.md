<pre>
    ___   __                     __  __                            
   /   | / /_____  ____ ___     / / / /____  ____  ____  ___  _____
  / /| |/ __/ __ \/ __ `__ \   / /_/ // __ \/ __ \/ __ \/ _ \/ ___/
 / ___ / /_/ /_/ / / / / / /  / __  // /_/ / /_/ / /_/ /  __/ /    
/_/  |_\__/\____/_/ /_/ /_/  /_/ /_/ \____/ .___/ .___/\___/_/     
                                         /_/   /_/                 
</pre>

#How to switch between the different adapters like Hibernate adapter and JDBC adapter.#

Steps:

* Rename atom-server.cfg.xml to atom-server_Hibernate.cfg.xml present at location /atom-hopper/test-suite/src/test/resources/META-INF/
* Rename atom-server_JDBC.cfg.xml to atom-server.cfg.xml present at location /atom-hopper/test-suite/src/test/resources/META-INF/
* Rename application-context.xml to application-context_Hibernate.xml present at location atom-hopper/test-suite/src/main/resources/META-INF
* Rename application-context_JDBC.xml to application-context.xml present at location atom-hopper/test-suite/src/main/resources/META-INF