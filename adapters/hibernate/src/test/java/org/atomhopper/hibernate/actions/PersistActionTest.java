package org.atomhopper.hibernate.actions;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

/**
 * User: sbrayman
 * Date: Sep 23, 2011
 */

@RunWith(Enclosed.class)
public class PersistActionTest {

    public static class WhenPersistingAnAction {
        private Session session;
        private Object persistMe;
        private PersistAction persistAction;

        @Before
        public void standUp() {
            session = mock(Session.class);
            persistMe = mock(Object.class);
            persistAction = new PersistAction(persistMe);
        }

        @Test
        public void shouldPersistSession() throws Exception {
            persistAction.perform(session);
            verify(session, only()).persist(persistMe);
        }
    }
}
