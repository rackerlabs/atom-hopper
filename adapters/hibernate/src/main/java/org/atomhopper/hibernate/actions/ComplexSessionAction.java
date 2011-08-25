package org.atomhopper.hibernate.actions;

import org.hibernate.Session;

public interface ComplexSessionAction<T> {

    T perform(Session liveSession);
}
