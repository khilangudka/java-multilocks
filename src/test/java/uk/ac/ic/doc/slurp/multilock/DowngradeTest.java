package uk.ac.ic.doc.slurp.multilock;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.ic.doc.slurp.multilock.LockMode.*;

/**
 * Tests which check the facility for downgrading the
 * mode of a lock from a stronger to a weaker mode.
 *
 * We use a separate thread, to avoid locking up test
 * suite execution if downgrading is blocked.
 *
 * For each test, a new thread proceeds as follows:
 * 1. Acquire the lock for the strong mode
 * 2. Acquire the lock for the weak mode
 * 3. Release the lock for the strong mode
 *
 * If the above cannot be executed within a defined
 * timeout, then downgrading is considered to be
 * impossible.
 *
 * @author Adam Retter <adam@evolvedbinary.com>
 */
public class DowngradeTest {

    private static final long LOCK_ACQUISITION_TIMEOUT = 40;        // TODO(AR) this might need to be longer on slower machines...

    @Test
    public void downgrade_IX_IS() throws InterruptedException, ExecutionException {
        assertDowngradable(IX, IS);
    }

    @Test
    public void downgrade_S_IS() throws InterruptedException, ExecutionException {
        assertDowngradable(S, IS);
    }

    @Test
    public void downgrade_SIX_IX() throws InterruptedException, ExecutionException {
        assertDowngradable(SIX, IX);
    }

    @Test
    public void downgrade_SIX_S() throws InterruptedException, ExecutionException {
        assertDowngradable(SIX, S);
    }

    @Test
    public void downgrade_SIX_IS() throws InterruptedException, ExecutionException {
        assertDowngradable(SIX, IS);
    }

    @Test
    public void downgrade_X_SIX() throws InterruptedException, ExecutionException {
        assertDowngradable(X, SIX);
    }

    @Test
    public void downgrade_X_IX() throws InterruptedException, ExecutionException {
        assertDowngradable(X, IX);
    }

    @Test
    public void downgrade_X_S() throws InterruptedException, ExecutionException {
        assertDowngradable(X, S);
    }

    @Test
    public void downgrade_X_IS() throws InterruptedException, ExecutionException {
        assertDowngradable(X, IS);
    }

    private static void assertDowngradable(final LockMode from, final LockMode to) throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(Arrays.asList(new Downgrade(multiLock, from, to)), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertTrue(future.get());
        }
    }

    private static class Downgrade implements Callable<Boolean> {
        private final MultiLock multiLock;
        private final LockMode from;
        private final LockMode to;

        private Downgrade(final MultiLock multiLock, final LockMode from, final LockMode to) {
            this.multiLock = multiLock;
            this.from = from;
            this.to = to;
        }

        @Override
        public Boolean call() {
            if (from.lock(multiLock)) {
                if (to.lock(multiLock)) {
                    from.unlock(multiLock);

                    return true;
                }
            }
            return false;
        }
    }
}
