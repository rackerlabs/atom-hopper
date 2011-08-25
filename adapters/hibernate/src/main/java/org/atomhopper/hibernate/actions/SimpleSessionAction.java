package org.atomhopper.hibernate.actions;

import org.hibernate.Session;

public interface SimpleSessionAction {

    void perform(Session liveSession);
}
