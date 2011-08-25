package org.hopperadapter.hibernate.impl;

import org.hopperadapter.hibernate.actions.SessionAction;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hopperadapter.hibernate.actions.PersistAction;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class HibernateFeedRepository implements FeedRepository {
    
    private final HibernateSessionManager sessionManager;
    
    public static void main(String[] args) {
        final HibernateFeedRepository feedRepository = new HibernateFeedRepository(Collections.EMPTY_MAP);
        
        feedRepository.addFeed("testing");
        Feed f = feedRepository.getFeed("testing");
        
        System.out.println(f != null ? f.getName() : "null");
        
        FeedEntry entry = new FeedEntry("some-random-uuid");
        entry.setFeed(new Feed("testing"));
        
        feedRepository.addEntry(entry);
        
        feedRepository.performAction(new SessionAction() {
            
            @Override
            public void perform(Session liveSession) {
                Feed f = (Feed) liveSession.createCriteria(Feed.class).add(Restrictions.idEq("testing")).list().get(0);
                
                System.out.println("Entries: " + (f != null ? f.getEntries().size() : 0));
            }
        });
    }
    
    public HibernateFeedRepository(Map<String, String> parameters) {
        sessionManager = new HibernateSessionManager(parameters);
    }
    
    private void performAction(SessionAction action) {
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
    public void addFeed(String feedName) {
        performAction(new PersistAction(new Feed(feedName)));
    }
    
    @Override
    public void addEntry(FeedEntry entry) {
        performAction(new PersistAction(entry));
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
