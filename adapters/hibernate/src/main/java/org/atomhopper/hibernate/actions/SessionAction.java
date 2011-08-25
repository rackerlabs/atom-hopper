package org.atomhopper.hibernate.actions;

import org.hibernate.Session;

public interface SessionAction {

    void perform(Session liveSession);
}
