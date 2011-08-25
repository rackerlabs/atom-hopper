package org.hopperadapter.hibernate.actions;

import org.hibernate.Session;

public interface SessionAction {

    void perform(Session liveSession);
}
