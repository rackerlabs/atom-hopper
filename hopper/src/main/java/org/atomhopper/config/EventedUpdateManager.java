package org.atomhopper.config;

import com.rackspace.papi.commons.config.manager.ConfigurationUpdateManager;
import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.commons.config.parser.common.ConfigurationParser;
import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.commons.util.thread.DestroyableThreadWrapper;
import com.rackspace.papi.commons.util.thread.Poller;
import com.rackspace.papi.service.config.impl.ConfigurationEvent;
import com.rackspace.papi.service.config.impl.ConfigurationResourceWatcher;
import com.rackspace.papi.service.config.impl.ParserListenerPair;
import com.rackspace.papi.service.config.impl.PowerApiUpdateManagerEventListener;
import com.rackspace.papi.service.event.common.EventListener;
import com.rackspace.papi.service.event.common.EventService;
import com.rackspace.papi.service.threading.ThreadingService;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zinic
 */
public class EventedUpdateManager implements ConfigurationUpdateManager {

   private final Map<String, Map<Integer, ParserListenerPair>> listenerMap;
   private final EventService eventManager;
   private final EventListener<ConfigurationEvent, ConfigurationResource> eventListener;
   private final ConfigurationResourceWatcher resourceWatcher;
   private DestroyableThreadWrapper resrouceWatcherThread;

   public EventedUpdateManager(EventService eventManager) {
      this.eventManager = eventManager;
      resourceWatcher = new ConfigurationResourceWatcher(eventManager);
      listenerMap = new HashMap<String, Map<Integer, ParserListenerPair>>();
      eventListener = new PowerApiUpdateManagerEventListener(listenerMap);
   }

   public void start(ThreadingService threadingService) {
      final Poller pollerLogic = new Poller(resourceWatcher, 15000);

      resrouceWatcherThread = new DestroyableThreadWrapper(threadingService.newThread(pollerLogic, "Configuration Watcher Thread"), pollerLogic);
      resrouceWatcherThread.start();

      // Listen for configuration events
      eventManager.listen(eventListener, ConfigurationEvent.class);
   }

   @Override
   public synchronized void destroy() {
      resrouceWatcherThread.destroy();
      listenerMap.clear();
   }

   @Override
   public synchronized <T> void registerListener(UpdateListener<T> listener, ConfigurationResource resource, ConfigurationParser<T> parser) {
      Map<Integer, ParserListenerPair> resourceListeners = listenerMap.get(resource.name());

      if (resourceListeners == null) {
         resourceListeners = new HashMap<Integer, ParserListenerPair>();

         listenerMap.put(resource.name(), resourceListeners);
         resourceWatcher.watch(resource);
      }

      resourceListeners.put(listener.hashCode(), new ParserListenerPair(listener, parser));
   }

   @Override
   public synchronized <T> void unregisterListener(UpdateListener<T> listener, ConfigurationResource resource) {
      Map<Integer, ParserListenerPair> resourceListeners = listenerMap.get(resource.name());

      if (resourceListeners != null) {
         resourceListeners.remove(listener.hashCode());

         if (resourceListeners.isEmpty()) {
            resourceWatcher.stopWatching(resource.name());
            listenerMap.remove(resource.name());
         }
      }
   }
}