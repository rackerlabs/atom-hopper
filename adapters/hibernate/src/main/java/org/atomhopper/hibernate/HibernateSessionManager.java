package org.atomhopper.hibernate;

import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Map;

public class HibernateSessionManager implements SessionManager {

    private final SessionFactory sessionFactory;

    public HibernateSessionManager(Map<String, String> parameters) {
        sessionFactory = buildSessionFactory(parameters);
    }

    private static SessionFactory buildSessionFactory(Map<String, String> parameters) {
        final Configuration hibernateConfiguration = new Configuration()
                .addAnnotatedClass(PersistedFeed.class)
                .addAnnotatedClass(PersistedEntry.class)
                .addAnnotatedClass(PersistedCategory.class);

        for (Map.Entry<String, String> userParameter : parameters.entrySet()) {
            hibernateConfiguration.setProperty(userParameter.getKey(), userParameter.getValue());
        }

        return hibernateConfiguration.buildSessionFactory();
    }

   @Override
    public Session getSession() {
        return sessionFactory.openSession();
    }
}
