package org.hopperadapter.hibernate.actions;

import org.hibernate.Session;

public class PersistAction implements SessionAction {

    private final Object persistMe;

    public PersistAction(Object persistMe) {
        this.persistMe = persistMe;
    }

    @Override
    public void perform(Session liveSession) {
        liveSession.persist(persistMe);
    }
}
