package uk.ac.ic.doc.slurp.multilock;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.ic.doc.slurp.multilock.LockMode.*;

/**
 * Tests which check that acquiring the lock
 * for a mode is re-entrant / recusive.
 *
 * We use a separate thread, to avoid locking up test
 * suite execution if lock acquisition is not reentrant.
 *
 * @author Adam Retter <adam@evolvedbinary.com>
 */
public class ReentrancyTest {

    private static final int REENTER_COUNT = 10;
    private static final long LOCK_ACQUISITIONS_TIMEOUT = 20 * REENTER_COUNT;        // TODO(AR) this might need to be longer on slower machines...

    @Test
    public void reentrant_IS() throws InterruptedException, ExecutionException {
        assertReentrant(IS);
    }

    @Test
    public void reentrant_IS_interruptibly() throws InterruptedException, ExecutionException {
        assertReentrantInterruptibly(IS);
    }

    @Test
    public void reentrant_IX() throws InterruptedException, ExecutionException {
        assertReentrant(IX);
    }

    @Test
    public void reentrant_IX_interruptibly() throws InterruptedException, ExecutionException {
        assertReentrantInterruptibly(IX);
    }

    @Test
    public void reentrant_S() throws InterruptedException, ExecutionException {
        assertReentrant(S);
    }

    @Test
    public void reentrant_S_interruptibly() throws InterruptedException, ExecutionException {
        assertReentrantInterruptibly(S);
    }

    @Test
    public void reentrant_SIX() throws InterruptedException, ExecutionException {
        assertReentrant(SIX);
    }

    @Test
    public void reentrant_SIX_interruptibly() throws InterruptedException, ExecutionException {
        assertReentrantInterruptibly(SIX);
    }

    @Test
    public void reentrant_X() throws InterruptedException, ExecutionException {
        assertReentrant(X);
    }

    @Test
    public void reentrant_X_interruptibly() throws InterruptedException, ExecutionException {
        assertReentrantInterruptibly(X);
    }

    private static void assertReentrant(final LockMode lockMode) throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Integer>> futures = executorService.invokeAll(Arrays.asList(new Reenter(multiLock, lockMode, REENTER_COUNT)), LOCK_ACQUISITIONS_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Integer> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertEquals(REENTER_COUNT, (int)future.get());
        }
    }

    private static void assertReentrantInterruptibly(final LockMode lockMode) throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Integer>> futures = executorService.invokeAll(Arrays.asList(new ReenterInterruptibly(multiLock, lockMode, REENTER_COUNT)), LOCK_ACQUISITIONS_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Integer> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertEquals(REENTER_COUNT, (int)future.get());
        }
    }

    private static class Reenter implements Callable<Integer> {
        private final MultiLock multiLock;
        private final LockMode lockMode;
        private final int count;

        private Reenter(final MultiLock multiLock, final LockMode lockMode, final int count) {
            this.multiLock = multiLock;
            this.lockMode = lockMode;
            this.count = count;
        }

        @Override
        public Integer call() {
            int success = 0;
            for (int i = 0; i < count; i++) {
                if (lockMode.lock(multiLock)) {
                    success++;
                }
            }
            return success;
        }
    }

    private static class ReenterInterruptibly implements Callable<Integer> {
        private final MultiLock multiLock;
        private final LockMode lockMode;
        private final int count;

        private ReenterInterruptibly(final MultiLock multiLock, final LockMode lockMode, final int count) {
            this.multiLock = multiLock;
            this.lockMode = lockMode;
            this.count = count;
        }

        @Override
        public Integer call() throws InterruptedException {
            int success = 0;
            for (int i = 0; i < count; i++) {
                lockMode.lockInterruptibly(multiLock);
                success++;
            }
            return success;
        }
    }
}
