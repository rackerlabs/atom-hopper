package org.atomhopper.config;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.config.v1_0.*;

/**
 *
 * @author zinic
 */
public class AtomHopperConfigurationPreprocessor {

   private final Configuration config;

   public AtomHopperConfigurationPreprocessor(Configuration config) {
      this.config = config;
   }

   public AtomHopperConfigurationPreprocessor applyDefaults() {
      final ConfigurationDefaults defaults = config.getDefaults();
      setDefaultAuthor(config.getWorkspace(), defaults.getAuthor());

      return new AtomHopperConfigurationPreprocessor(config);
   }

   public Configuration getConfig() {
      return config;
   }

   private void setDefaultAuthor(List<WorkspaceConfiguration> workspaces, Author globalAuthorDefault) {
      for (WorkspaceConfiguration workspace : workspaces) {
         final Author workspaceAuthorDefault = workspace.getDefaults().getAuthor();
         final Author authorToApply = isAuthorEmpty(workspaceAuthorDefault)
                 ? globalAuthorDefault : workspaceAuthorDefault;

         if (!isAuthorEmpty(authorToApply)) {
            for (FeedConfiguration feed : workspace.getFeed()) {
               if (isAuthorEmpty(feed.getAuthor())) {
                  feed.setAuthor(globalAuthorDefault);
               }
            }
         }
      }
   }

   private boolean isAuthorEmpty(Author author) {
      return author == null && StringUtils.isBlank(author.getName());
   }
}
