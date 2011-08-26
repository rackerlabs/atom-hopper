package org.atomhopper.hibernate;

import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import java.util.Collection;
import java.util.Map;
import org.atomhopper.adapter.jpa.Category;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;
import org.atomhopper.dbal.AtomDatabaseException;
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
            if (tx != null) {
                tx.rollback();
            }

            throw new AtomDatabaseException("Failure performing hibernate action: " + action.toString(), ex);
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
            if (tx != null) {
                tx.rollback();
            }

            throw new AtomDatabaseException("Failure performing hibernate action: " + action.toString(), ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void saveFeed(final String feedName) {
        performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                liveSession.persist(new Feed(feedName));
            }
        });
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
        return performComplexAction(new ComplexSessionAction<Collection<Feed>>() {

            @Override
            public Collection<Feed> perform(Session liveSession) {
                return liveSession.createCriteria(Feed.class).list();
            }
        });
    }

    @Override
    public FeedEntry getEntry(final String entryId) {
        return performComplexAction(new ComplexSessionAction<FeedEntry>() {

            @Override
            public FeedEntry perform(Session liveSession) {
                return (FeedEntry) liveSession.createCriteria(FeedEntry.class).add(Restrictions.idEq(entryId)).uniqueResult();
            }
        });
    }

    @Override
    public Feed getFeed(final String name) {
        return performComplexAction(new ComplexSessionAction<Feed>() {

            @Override
            public Feed perform(Session liveSession) {
                return (Feed) liveSession.createCriteria(Feed.class).add(Restrictions.idEq(name)).uniqueResult();
            }
        });
    }
}
