package net.jps.atom.hopper.adapter.hibernate.impl;

import net.jps.atom.hopper.adapter.hibernate.impl.domain.Category;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.Feed;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.FeedEntry;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Don't mind my fussing around here >_>
 *  - Hibernate and I haven't played in ages.
 */
public class HibernateSessionManager {
    
    public static void main(String[] args) {
        final SessionFactory sessionFactory = generateDefaultSessionFactory();
    }
    
    public static SessionFactory generateDefaultSessionFactory() {
        final Configuration hibernateConfiguration = new Configuration()
                .addAnnotatedClass(Feed.class)
                .addAnnotatedClass(FeedEntry.class)
                .addAnnotatedClass(Category.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:~/atom-hopper-db")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        
        return hibernateConfiguration.buildSessionFactory();
    }
}
