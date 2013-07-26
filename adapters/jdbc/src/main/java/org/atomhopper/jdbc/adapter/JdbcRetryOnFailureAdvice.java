package org.atomhopper.jdbc.adapter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * This class is an Around Advice meant to surround a call to the JdbcTemplate accessing
 * a Postgres database.
 *
 * User: shin4590
 * Date: 7/23/13
 * Time: 4:21 PM
 */
public class JdbcRetryOnFailureAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcRetryOnFailureAdvice.class);

    private String sqlStatesToRetry = "";

    private int maxRetriesOnConnDrop = 0;

    private long retryWaitInMillis = 1000;

    public Object retryOnFailure(ProceedingJoinPoint jp) throws Throwable {
        Object result = null;
        int retryCount = 0;
        do {
            try {
                Object[] args = jp.getArgs();
                if ( args != null && args.length > 0 )
                    result = jp.proceed(jp.getArgs());
                else
                    result = jp.proceed();
                break;
            } catch (DataAccessResourceFailureException ex) {
                if ( ex.getCause() instanceof PSQLException ) {
                    PSQLException psqlEx = (PSQLException) ex.getCause();
                    String sqlState = psqlEx.getSQLState();
                    LOG.warn("Got exception with sqlState=" + sqlState + ": " + ex);
                    if ( sqlStatesToRetry.contains(sqlState) && retryCount < maxRetriesOnConnDrop ) {
                        LOG.warn("Retrying...(retryCount=" + retryCount + ")");
                        sleep(retryWaitInMillis);
                    } else {
                        throw ex;
                    }
                }
            }
            retryCount++;
        } while ( retryCount <= maxRetriesOnConnDrop );
        return result;
    }

    public String getSqlStatesToRetry() {
        return sqlStatesToRetry;
    }

    public void setSqlStatesToRetry(String sqlStatesToRetry) {
        this.sqlStatesToRetry = sqlStatesToRetry;
    }

    public int getMaxRetriesOnConnDrop() {
        return maxRetriesOnConnDrop;
    }

    public void setMaxRetriesOnConnDrop(int maxRetriesOnConnDrop) {
        this.maxRetriesOnConnDrop = maxRetriesOnConnDrop;
    }

    public long getRetryWaitInMillis() {
        return retryWaitInMillis;
    }

    public void setRetryWaitInMillis(long retryWaitInMillis) {
        this.retryWaitInMillis = retryWaitInMillis;
    }

    private void sleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch(Exception ex) {
            // ignore
        }
    }
}
