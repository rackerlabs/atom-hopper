package org.atomhopper.hibernate;

import java.util.Map;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;


public class HibernateSessionManager {

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    public HibernateSessionManager(Map<String, String> parameters) {
        sessionFactory = configureSessionFactory(parameters);
    }

    private static SessionFactory configureSessionFactory(Map<String, String> parameters) throws HibernateException {
        final Configuration hibernateConfiguration = new Configuration()
                .addAnnotatedClass(PersistedFeed.class)
                .addAnnotatedClass(PersistedEntry.class)
                .addAnnotatedClass(PersistedCategory.class);

        for (Map.Entry<String, String> userParameter : parameters.entrySet()) {
            hibernateConfiguration.setProperty(userParameter.getKey(), userParameter.getValue());
        }

        serviceRegistry = new ServiceRegistryBuilder().applySettings(hibernateConfiguration.getProperties()).buildServiceRegistry();
        sessionFactory = hibernateConfiguration.buildSessionFactory(serviceRegistry);

        return sessionFactory;
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }
}
