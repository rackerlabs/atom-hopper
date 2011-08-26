package org.atomhopper.hibernate;

import java.util.Map;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionManager {

    private final Map<String, String> parameters;
    private SessionFactory sessionFactory;

    public HibernateSessionManager(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    private static SessionFactory buildSessionFactory(Map<String, String> parameters) {
        final Configuration hibernateConfiguration = new Configuration()
                .addAnnotatedClass(PersistedFeed.class)
                .addAnnotatedClass(PersistedEntry.class)
                .addAnnotatedClass(PersistedCategory.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:~/atom-hopper-db")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "create")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        return hibernateConfiguration.buildSessionFactory();
    }

    public synchronized Session getSession() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory(parameters);
        }
        
        return sessionFactory.openSession();
    }
}
