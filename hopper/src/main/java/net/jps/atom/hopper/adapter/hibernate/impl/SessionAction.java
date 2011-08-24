package net.jps.atom.hopper.adapter.hibernate.impl;

import org.hibernate.Session;

public interface SessionAction {

    void perform(Session liveSession);
}
