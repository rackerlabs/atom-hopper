/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atomhopper.hibernate;

import java.util.Collections;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.hibernate.actions.SimpleSessionAction;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author zinic
 */
public class HibernateFeedRepositoryTest {

    public static void main(String[] args) {
        final HibernateFeedRepository feedRepository = new HibernateFeedRepository(Collections.EMPTY_MAP);

        feedRepository.saveFeed(new PersistedFeed("testing", "uuid:not-really"));
        PersistedFeed f = feedRepository.getFeed("testing");

        System.out.println(f != null ? f.getName() : "null");

        PersistedEntry entry = new PersistedEntry("some-random-uuid");
        entry.setFeed(new PersistedFeed("testing", "uuid:not-really"));

        feedRepository.saveEntry(entry);

        feedRepository.performSimpleAction(new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
                PersistedFeed f = (PersistedFeed) liveSession.createCriteria(PersistedFeed.class).add(Restrictions.idEq("testing")).list().get(0);

                System.out.println("Entries: " + (f != null ? f.getEntries().size() : 0));
            }
        });
    }
}
