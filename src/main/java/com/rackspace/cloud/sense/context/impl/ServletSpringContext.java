/*
 *  Copyright 2010 Rackspace.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.context.impl;

import com.rackspace.cloud.sense.context.ApplicationContextAdapter;
import javax.servlet.ServletContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author John Hopper
 */
public class ServletSpringContext implements ApplicationContextAware, ApplicationContextAdapter {

    private ApplicationContext applicationContext;

    @Override
    public synchronized void setApplicationContext(ApplicationContext ac) throws BeansException {
        if (applicationContext == null) {
            applicationContext = ac;
        }
    }

    @Override
    public synchronized void usingServletContext(ServletContext context) {
        if (applicationContext == null) {
            applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        }
    }

    @Override
    public <T> T fromContext(Class<T> classToCastTo) {
        return applicationContext.getBean(classToCastTo);
    }

    @Override
    public <T> T fromContext(String refName, Class<T> classToCastTo) {
        return applicationContext.getBean(refName, classToCastTo);
    }
}
