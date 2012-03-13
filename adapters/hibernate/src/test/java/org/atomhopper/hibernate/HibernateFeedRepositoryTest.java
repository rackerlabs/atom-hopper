package org.atomhopper.hibernate;

import org.atomhopper.dbal.AtomDatabaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import org.atomhopper.hibernate.actions.SimpleSessionAction;
import org.hibernate.Session;
import org.hibernate.Transaction;

import static org.mockito.Mockito.*;

/**
 * User: sbrayman Date: 2/27/12
 */
@RunWith(Enclosed.class)
public class HibernateFeedRepositoryTest {

   public static final SimpleSessionAction NOP_SIMPLE_ACTION = new SimpleSessionAction() {

      @Override
      public void perform(Session liveSession) {
      }
   };

   public static class WhenPerformingSimpleAction {

      protected HibernateFeedRepository feedRepository;
      protected Transaction mockedTransaction;
      protected Session mockedSession;

      @Before
      public void setup() throws Exception {
         mockedTransaction = mock(Transaction.class);

         mockedSession = mock(Session.class);
         when(mockedSession.beginTransaction()).thenReturn(mockedTransaction);

         final SessionManager sessionManager = mock(SessionManager.class);
         when(sessionManager.getSession()).thenReturn(mockedSession);

         feedRepository = new HibernateFeedRepository(sessionManager);
      }

      @Test(expected = AtomDatabaseException.class)
      public void shouldHandleExceptionsThrownFromAction() {
         final SimpleSessionAction myAction = new SimpleSessionAction() {

            @Override
            public void perform(Session liveSession) {
               throw new RuntimeException("Failure occured.");
            }
         };

         try {
            feedRepository.performSimpleAction(myAction);
         } finally {
            verify(mockedTransaction, times(1)).rollback();
            verify(mockedSession, times(1)).close();
         }
      }

      @Test(expected = AtomDatabaseException.class)
      public void shouldHandleExceptionsInStartingTransactions() {
         when(mockedSession.beginTransaction()).thenThrow(new RuntimeException("Failure in starting transaction"));

         feedRepository.performSimpleAction(NOP_SIMPLE_ACTION);
      }

      @Test
      public void shouldPerformSimpleAction() {
         feedRepository.performSimpleAction(NOP_SIMPLE_ACTION);

         verify(mockedSession, times(1)).beginTransaction();
         verify(mockedTransaction, times(1)).commit();
         verify(mockedSession, times(1)).close();
      }
   }
}
