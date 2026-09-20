package org.yeastrc.db;

import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;

import junit.framework.TestCase;

/**
 * isRetryable must recognise a deadlock and a lock wait timeout, and nothing else.  Reporting an
 * ordinary failure as retryable would send the user back to submit a request that cannot succeed.
 */
public class DbErrorUtilsTest extends TestCase
{
    private static final String OTHER_MESSAGE = "There was an error saving usage block.";

    /** SQLState 23000, error 1062, the duplicate key MariaDB returns on a unique index. */
    private static SQLException duplicateKey()
    {
        return new SQLException("Duplicate entry '1' for key 'PRIMARY'", "23000", 1062);
    }

    /** What MySQL Connector/J returns to the transaction chosen as the deadlock victim. */
    private static SQLException deadlock()
    {
        return new SQLTransactionRollbackException(
                "Deadlock found when trying to get lock; try restarting transaction", "40001", 1213);
    }

    public final void testDeadlockSqlState() {
        assertTrue("A failure carrying SQLState 40001 should be retryable",
                DbErrorUtils.isRetryable(new SQLException("Deadlock found", "40001", 0)));
    }

    public final void testDeadlockErrorCode() {
        assertTrue("A failure carrying MariaDB error 1213 should be retryable",
                DbErrorUtils.isRetryable(new SQLException("Deadlock found", "HY000", 1213)));
    }

    public final void testLockWaitTimeout() {
        assertTrue("A failure carrying MariaDB error 1205 should be retryable",
                DbErrorUtils.isRetryable(new SQLException("Lock wait timeout exceeded", "HY000", 1205)));
    }

    public final void testTransactionRollbackException() {
        assertTrue("A SQLTransactionRollbackException should be retryable",
                DbErrorUtils.isRetryable(deadlock()));
    }

    public final void testDuplicateKeyIsNotRetryable() {
        assertFalse("A duplicate key should not be retryable",
                DbErrorUtils.isRetryable(duplicateKey()));
    }

    public final void testSyntaxErrorIsNotRetryable() {
        assertFalse("A SQL syntax error should not be retryable",
                DbErrorUtils.isRetryable(new SQLException("You have an error in your SQL syntax", "42000", 1064)));
    }

    public final void testRuntimeExceptionIsNotRetryable() {
        assertFalse("A failure that is not a SQLException should not be retryable",
                DbErrorUtils.isRetryable(new IllegalStateException("No project found")));
    }

    public final void testNullIsNotRetryable() {
        assertFalse("A null failure should not be retryable", DbErrorUtils.isRetryable(null));
    }

    public final void testWrappedDeadlock() {
        assertTrue("A deadlock wrapped in another exception should be retryable",
                DbErrorUtils.isRetryable(new RuntimeException("Error saving usage blocks", deadlock())));
    }

    public final void testDeadlockWrappedTwice() {
        Throwable wrapped = new RuntimeException("Error exporting data",
                new SQLException("Error saving invoice block", "HY000", 0, deadlock()));
        assertTrue("A deadlock two levels down the cause chain should be retryable",
                DbErrorUtils.isRetryable(wrapped));
    }

    public final void testDeadlockOnTheNextException() {
        SQLException first = duplicateKey();
        first.setNextException(deadlock());
        assertTrue("A deadlock reached through getNextException should be retryable",
                DbErrorUtils.isRetryable(first));
    }

    public final void testWrappedDuplicateKeyIsNotRetryable() {
        assertFalse("A duplicate key wrapped in another exception should not be retryable",
                DbErrorUtils.isRetryable(new RuntimeException("Error saving usage blocks", duplicateKey())));
    }

    /** A chain linked back to itself must not be walked forever, which would hang the request thread. */
    public final void testSelfLinkedChainTerminates() {
        SQLException self = duplicateKey();
        self.setNextException(self);
        assertSame("setNextException should link the exception to itself", self, self.getNextException());
        assertFalse("A self-linked chain of ordinary failures should not be retryable",
                DbErrorUtils.isRetryable(self));
    }

    /**
     * The walk follows both getCause and getNextException, so the number of exceptions it reaches grows
     * faster than the depth.  A deadlock must be found however deeply it is wrapped.
     */
    public final void testDeadlockUnderABranchingChain() {
        Throwable failure = deadlock();
        for(int layer = 0; layer < 20; layer++) {
            SQLException wrapper = new SQLException("layer " + layer, "HY000", 0, failure);
            // A sibling on each layer, so the walk branches instead of following one line of causes.
            wrapper.setNextException(duplicateKey());
            failure = new RuntimeException("wrapped " + layer, wrapper);
        }
        assertTrue("A deadlock under twenty branching layers should be retryable",
                DbErrorUtils.isRetryable(failure));
    }

    public final void testMessageForADeadlock() {
        assertEquals("A deadlock should be reported with the message asking for another attempt",
                DbErrorUtils.RETRY_MESSAGE, DbErrorUtils.messageFor(deadlock(), OTHER_MESSAGE));
    }

    public final void testMessageForAnOrdinaryFailure() {
        assertEquals("An ordinary failure should be reported with the caller's own message",
                OTHER_MESSAGE, DbErrorUtils.messageFor(duplicateKey(), OTHER_MESSAGE));
    }

    public final void testIsRetryMessage() {
        assertTrue("The message messageFor produces for a deadlock should be recognised",
                DbErrorUtils.isRetryMessage(DbErrorUtils.messageFor(deadlock(), OTHER_MESSAGE)));
    }

    public final void testIsRetryMessageOnAnOrdinaryMessage() {
        assertFalse("A caller's own message should not be recognised as the retry message",
                DbErrorUtils.isRetryMessage(OTHER_MESSAGE));
        assertFalse("A null message should not be recognised as the retry message",
                DbErrorUtils.isRetryMessage(null));
    }
}
