package net.jps.atom.hopper.servlet;

import javax.servlet.ServletContext;


/**
 *
 * @author jhopper
 */
public interface ApplicationContextAdapter {

    void usingServletContext(ServletContext context);

    <T> T fromContext(Class<T> classToCastTo);

    <T> T fromContext(String refName, Class<T> classToCastTo);
}