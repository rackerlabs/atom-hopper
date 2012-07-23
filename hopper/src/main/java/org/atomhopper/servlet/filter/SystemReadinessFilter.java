package org.atomhopper.servlet.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import org.atomhopper.config.ConfigurationManager;
import org.atomhopper.servlet.context.AtomHopperContextParameterKeys;

/**
 *
 * @author zinic
 */
public class SystemReadinessFilter implements Filter {

   private ConfigurationManager configurationManager;

   @Override
   public void init(FilterConfig filterConfig) throws ServletException {
      final ServletContext context = filterConfig.getServletContext();

      configurationManager = (ConfigurationManager) context.getAttribute(AtomHopperContextParameterKeys.CFG_MANAGER);
   }

   @Override
   public void destroy() {
      configurationManager = null;
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      if (!configurationManager.isConfigured()) {
         ((HttpServletResponse) response).setStatus(503);
      } else {
         chain.doFilter(request, response);
      }
   }
}
