package org.yeastrc.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * endTransactionAndClose must roll back before it restores autocommit, and must not restore autocommit
 * at all after a rollback that threw.  setAutoCommit(true) on a connection with an open transaction
 * commits that transaction, so either mistake writes the transaction the caller is discarding.
 */
public class DBConnectionManagerTest extends TestCase
{
    private static final String ROLLBACK = "rollback";
    private static final String RESTORE_AUTOCOMMIT = "setAutoCommit(true)";
    private static final String CLOSE = "close";

    /** A Connection that records the calls made to it, and can fail its rollback. */
    private static class RecordingConnection implements InvocationHandler
    {
        private final boolean rollbackFails;
        private final List<String> calls = new ArrayList<String>();

        RecordingConnection(boolean rollbackFails)
        {
            this.rollbackFails = rollbackFails;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            String name = method.getName();
            calls.add(args == null || args.length == 0 ? name : name + "(" + args[0] + ")");

            if(ROLLBACK.equals(name) && rollbackFails)
            {
                throw new SQLException("Rollback failed", "08003", 0);
            }
            return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        }

        Connection connection()
        {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[] { Connection.class }, this);
        }
    }

    public final void testFailedRollbackDoesNotRestoreAutocommit() {
        RecordingConnection recorder = new RecordingConnection(true);
        DBConnectionManager.endTransactionAndClose(recorder.connection(), false);

        assertTrue("A transaction that was not committed should be rolled back",
                recorder.calls.contains(ROLLBACK));
        assertFalse("Autocommit should not be restored after a rollback that failed, because that would"
                        + " commit the transaction still open on the connection",
                recorder.calls.contains(RESTORE_AUTOCOMMIT));
        assertTrue("The connection should still be closed", recorder.calls.contains(CLOSE));
    }

    public final void testAbandonedTransactionIsRolledBackBeforeAutocommitIsRestored() {
        RecordingConnection recorder = new RecordingConnection(false);
        DBConnectionManager.endTransactionAndClose(recorder.connection(), false);

        assertTrue("A transaction that was not committed should be rolled back",
                recorder.calls.contains(ROLLBACK));
        assertTrue("Autocommit should be restored once the rollback succeeded",
                recorder.calls.contains(RESTORE_AUTOCOMMIT));
        assertTrue("The rollback should come before autocommit is restored",
                recorder.calls.indexOf(ROLLBACK) < recorder.calls.indexOf(RESTORE_AUTOCOMMIT));
        assertTrue("The connection should be closed last",
                recorder.calls.indexOf(RESTORE_AUTOCOMMIT) < recorder.calls.indexOf(CLOSE));
    }

    public final void testCommittedTransactionIsNotRolledBack() {
        RecordingConnection recorder = new RecordingConnection(false);
        DBConnectionManager.endTransactionAndClose(recorder.connection(), true);

        assertFalse("A committed transaction should not be rolled back",
                recorder.calls.contains(ROLLBACK));
        assertTrue("Autocommit should be restored", recorder.calls.contains(RESTORE_AUTOCOMMIT));
        assertTrue("The connection should be closed", recorder.calls.contains(CLOSE));
    }

    public final void testNullConnection() {
        DBConnectionManager.endTransactionAndClose(null, false);
        DBConnectionManager.endTransactionAndClose(null, true);
    }
}
