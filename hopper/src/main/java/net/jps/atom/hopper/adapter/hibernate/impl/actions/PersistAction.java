package net.jps.atom.hopper.adapter.hibernate.impl.actions;

import net.jps.atom.hopper.adapter.hibernate.impl.SessionAction;
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
