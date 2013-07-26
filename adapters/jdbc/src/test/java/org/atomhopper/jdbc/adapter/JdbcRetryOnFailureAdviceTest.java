package org.atomhopper.jdbc.adapter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.mockito.Mockito.*;

/**
 * User: shin4590
 * Date: 7/23/13
 * Time: 8:48 PM
 */
@RunWith(Enclosed.class)
public class JdbcRetryOnFailureAdviceTest {

    public static class WhenPostingEntries {

        private JdbcRetryOnFailureAdvice jdbcRetryOnFailureAdvice;
        private ProceedingJoinPoint jointPoint;

        @Before
        public void setUp() throws Exception {

            jointPoint = mock(ProceedingJoinPoint.class);

            jdbcRetryOnFailureAdvice = new JdbcRetryOnFailureAdvice();
        }

        @Test
        public void shouldRetryWhenFailure() throws Throwable {
            jdbcRetryOnFailureAdvice.setMaxRetriesOnConnDrop(2);
            jdbcRetryOnFailureAdvice.setRetryWaitInMillis(10);
            jdbcRetryOnFailureAdvice.setSqlStatesToRetry(PSQLState.CONNECTION_UNABLE_TO_CONNECT.getState());

            when(jointPoint.getArgs()).thenReturn(null);
            when(jointPoint.proceed())
                                .thenThrow(new DataAccessResourceFailureException("foo", new PSQLException("foo", PSQLState.CONNECTION_UNABLE_TO_CONNECT)))
                                .thenReturn(null);
            jdbcRetryOnFailureAdvice.retryOnFailure(jointPoint);
            verify(jointPoint, times(2)).proceed();
        }

        @Test(expected=DataAccessResourceFailureException.class)
        public void shouldRetryUpToMaxRetries() throws Throwable {
            jdbcRetryOnFailureAdvice.setMaxRetriesOnConnDrop(2);
            jdbcRetryOnFailureAdvice.setRetryWaitInMillis(10);
            jdbcRetryOnFailureAdvice.setSqlStatesToRetry(PSQLState.CONNECTION_UNABLE_TO_CONNECT.getState());

            when(jointPoint.getArgs()).thenReturn(null);
            when(jointPoint.proceed())
                                .thenThrow(new DataAccessResourceFailureException("foo", new PSQLException("foo", PSQLState.CONNECTION_UNABLE_TO_CONNECT)))
                                .thenThrow(new DataAccessResourceFailureException("foo", new PSQLException("foo", PSQLState.CONNECTION_UNABLE_TO_CONNECT)))
                                .thenThrow(new DataAccessResourceFailureException("foo", new PSQLException("foo", PSQLState.CONNECTION_UNABLE_TO_CONNECT)));
            jdbcRetryOnFailureAdvice.retryOnFailure(jointPoint);
            verify(jointPoint, times(3)).proceed();
        }
    }
}
