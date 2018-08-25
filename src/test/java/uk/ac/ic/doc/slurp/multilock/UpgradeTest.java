package uk.ac.ic.doc.slurp.multilock;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.ic.doc.slurp.multilock.LockMode.*;

/**
 * Tests which check the facility for upgrading the
 * mode of a lock from a weaker to a stronger mode.
 *
 * We use a separate thread, to avoid locking up test
 * suite execution if upgrading is blocked.
 *
 * For each test, a new thread proceeds as follows:
 * 1. Acquire the lock for the weak mode
 * 2. Acquire the lock for the strong mode
 * 3. Release the lock for the weak mode
 *
 * If the above cannot be executed within a defined
 * timeout, then upgrading is considered to be
 * impossible.
 *
 * @author Adam Retter <adam@evolvedbinary.com>
 */
public class UpgradeTest {

    private static final long LOCK_ACQUISITION_TIMEOUT = 40;        // TODO(AR) this might need to be longer on slower machines...

    @Test
    public void upgrade_IS_IX() throws InterruptedException, ExecutionException {
        assertUpgradeable(IS, IX);
    }

    @Test
    public void upgrade_IS_S() throws InterruptedException, ExecutionException {
        assertUpgradeable(IS, S);
    }

    @Test
    public void upgrade_IS_SIX() throws InterruptedException, ExecutionException {
        assertUpgradeable(IS, SIX);
    }

    @Test
    public void upgrade_IS_X() throws InterruptedException, ExecutionException {
        assertUpgradeable(IS, X);
    }

    @Test
    public void upgrade_IX_SIX() throws InterruptedException, ExecutionException {
        assertUpgradeable(IX, SIX);
    }

    @Test
    public void upgrade_IX_X() throws InterruptedException, ExecutionException {
        assertUpgradeable(IX, X);
    }

    @Test
    public void upgrade_S_SIX() throws InterruptedException, ExecutionException {
        assertUpgradeable(S, SIX);
    }

    @Test
    public void upgrade_S_X() throws InterruptedException, ExecutionException {
        assertUpgradeable(S, X);
    }

    @Test
    public void upgrade_SIX_X() throws InterruptedException, ExecutionException {
        assertUpgradeable(SIX, X);
    }

    private static void assertUpgradeable(final LockMode from, final LockMode to) throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(Arrays.asList(new Upgrade(multiLock, from, to)), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertTrue(future.get());
        }
    }

    private static class Upgrade implements Callable<Boolean> {
        private final MultiLock multiLock;
        private final LockMode from;
        private final LockMode to;

        private Upgrade(final MultiLock multiLock, final LockMode from, final LockMode to) {
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
