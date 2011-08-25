package org.atomhopper.hibernate;

import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.atomhopper.adapter.jpa.Category;
import org.atomhopper.hibernate.actions.PersistAction;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;
import org.atomhopper.hibernate.actions.ComplexSessionAction;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class HibernateFeedRepository implements FeedRepository {

    private final HibernateSessionManager sessionManager;

    public HibernateFeedRepository(Map<String, String> parameters) {
        sessionManager = new HibernateSessionManager(parameters);
    }

    public void performSimpleAction(SimpleSessionAction action) {
        final Session session = sessionManager.getSession();

        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            action.perform(session);

            tx.commit();
        } catch (Exception ex) {
            //TODO: Log exception

            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
    }

    public <T> T performComplexAction(ComplexSessionAction<T> action) {
        final Session session = sessionManager.getSession();

        T returnable = null;
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            returnable = action.perform(session);

            tx.commit();

            return returnable;
        } catch (Exception ex) {
            //TODO: Log exception

            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return returnable;
    }

    private <T> T lookupUnique(Class<T> entityClass, String lookupCollumn, String value) {
        final Session session = sessionManager.getSession();

        try {
            final List<T> matchingFeedEntries = session.createCriteria(entityClass).add(Restrictions.eq(lookupCollumn, value)).list();

            if (!matchingFeedEntries.isEmpty()) {
                if (matchingFeedEntries.size() > 1) {
                    //TODO: Log DB consistency warning
                }

                return matchingFeedEntries.get(0);
            }
        } finally {
            session.close();
        }

        return null;
    }

    @Override
    public void saveFeed(String feedName) {
        performSimpleAction(new PersistAction(new Feed(feedName)));
    }

    @Override
    public void saveEntry(final FeedEntry entry) {
        performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                Feed feed = (Feed) liveSession.createCriteria(Feed.class).add(Restrictions.idEq(entry.getFeed().getName())).uniqueResult();

                if (feed == null) {
                    feed = entry.getFeed();
                }
                
                feed.getEntries().add(entry);
                
                liveSession.saveOrUpdate(feed);
                liveSession.persist(entry);

                // Make sure to update our category objects
                for (Category cat : entry.getCategories()) {
                    Category category = (Category) liveSession.createCriteria(Category.class).add(Restrictions.idEq(cat.getName())).uniqueResult();
                    
                    if (category == null) {
                        category = cat;
                    }

                    category.getFeedEntries().add(entry);
                    liveSession.saveOrUpdate(category);
                }
            }
        });
    }

    @Override
    public Collection<Feed> getAllFeeds() {
        final Session session = sessionManager.getSession();

        try {
            return session.createCriteria(Feed.class).list();
        } finally {
            session.close();
        }
    }

    @Override
    public FeedEntry getEntry(String entryId) {
        return lookupUnique(FeedEntry.class, "entryId", entryId);
    }

    @Override
    public Feed getFeed(String name) {
        return lookupUnique(Feed.class, "name", name);
    }
}
