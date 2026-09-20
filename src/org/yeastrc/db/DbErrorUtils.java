package org.yeastrc.db;

import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Recognises the database failures a user can recover from by submitting the same request again.
 *
 * InnoDB takes row locks, and with foreign keys it locks parent rows too.  Two requests that touch the
 * same project or block in a different order can deadlock.  MariaDB rolls one of them back and returns
 * error 1213, SQLState 40001.  A lock wait timeout returns error 1205 and rolls back only the statement,
 * leaving the transaction for the finally block of the site that opened it.
 *
 * Both end with the transaction rolled back and the database as it was, and the same request usually
 * succeeds on a second attempt.  Nothing retries on its own, because a retry would not start from
 * the values the first attempt started from.  InstrumentUsageDAO.saveUsageBlocksForBilledProject
 * copies the generated IDs back into the blocks it was given.  InvoiceBlockCreator.exportDone reads
 * a block's ID before that call, to find the block it was split from.  The user is told the request
 * can be submitted again.
 */
public class DbErrorUtils
{
    /** MariaDB error 1213, returned to the transaction chosen as the deadlock victim. */
    public static final int DEADLOCK_ERROR_CODE = 1213;

    /** MariaDB error 1205, returned when a statement waits for a row lock for longer than the timeout. */
    public static final int LOCK_WAIT_TIMEOUT_ERROR_CODE = 1205;

    /** SQLState 40001, the standard state for a transaction rolled back to break a deadlock. */
    public static final String TRANSACTION_ROLLBACK_SQL_STATE = "40001";

    /** Displayed for a deadlock or a lock wait timeout, in place of the driver's message. */
    public static final String RETRY_MESSAGE =
            "The database was busy with another request, so this one was cancelled and nothing was "
            + "saved.  Please submit it again.";

    /** An exception chain can link back into itself.  Stop after this many links. */
    private static final int MAX_CHAIN_LENGTH = 25;

    private DbErrorUtils() {}

    /**
     * Returns true when the failure, or anything it wraps, is a deadlock or a lock wait timeout.
     */
    public static boolean isRetryable(Throwable failure)
    {
        if(failure == null)
        {
            return false;
        }

        // A SQLException reaches a caller wrapped in whatever that layer throws.  It can also carry a
        // chain of further SQLExceptions.  Follow both getCause and getNextException.
        Deque<Throwable> pending = new ArrayDeque<Throwable>();
        pending.add(failure);

        for(int seen = 0; !pending.isEmpty() && seen < MAX_CHAIN_LENGTH; seen++)
        {
            Throwable t = pending.poll();

            if(t instanceof SQLException)
            {
                SQLException sqle = (SQLException) t;
                if(carriesRetryableCode(sqle))
                {
                    return true;
                }
                if(sqle.getNextException() != null)
                {
                    pending.add(sqle.getNextException());
                }
            }

            if(t.getCause() != null)
            {
                pending.add(t.getCause());
            }
        }

        return false;
    }

    /**
     * Returns the message to display for a failed request.  A deadlock or a lock wait timeout gets
     * RETRY_MESSAGE, and every other failure gets otherMessage unchanged.
     */
    public static String messageFor(Throwable failure, String otherMessage)
    {
        return isRetryable(failure) ? RETRY_MESSAGE : otherMessage;
    }

    private static boolean carriesRetryableCode(SQLException sqle)
    {
        if(sqle instanceof SQLTransactionRollbackException)
        {
            return true;
        }

        int errorCode = sqle.getErrorCode();
        if(errorCode == DEADLOCK_ERROR_CODE || errorCode == LOCK_WAIT_TIMEOUT_ERROR_CODE)
        {
            return true;
        }

        return TRANSACTION_ROLLBACK_SQL_STATE.equals(sqle.getSQLState());
    }
}
