package org.atomhopper.abdera;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ThreadSafeWorkspaceManager implements org.apache.abdera.protocol.server.WorkspaceManager {

   private final List<WorkspaceHandler> handlers;

   public ThreadSafeWorkspaceManager() {
      handlers = new LinkedList<WorkspaceHandler>();
   }

   public synchronized void clearWorkspaces() {
      handlers.clear();
   }
   
   public synchronized void addWorkspaces(List<WorkspaceHandler> workspaces) {
      handlers.addAll(workspaces);
   }

   @Override
   public synchronized Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
      return (Collection) Collections.unmodifiableCollection(handlers);
   }

   @Override
   public synchronized CollectionAdapter getCollectionAdapter(RequestContext request) {
      for (WorkspaceHandler workspace : handlers) {
         final CollectionAdapter adapter = workspace.getAnsweringAdapter(request);

         if (adapter != null) {
            return adapter;
         }
      }

      return null;
   }
}
