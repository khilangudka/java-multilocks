package uk.ac.ic.doc.slurp.multilock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    // TODO(AR) this might need to be longer on slower machines...
    private static final long LOCK_ACQUISITION_TIMEOUT = 40;

    static List<Arguments> upgradeModesProvider() {
        return Arrays.asList(
                Arguments.of(IS,    IX),
                Arguments.of(IS,    S),
                Arguments.of(IS,    SIX),
                Arguments.of(IS,    X),
                Arguments.of(IX,    SIX),
                Arguments.of(IX,    X),
                Arguments.of(S,     SIX),
                Arguments.of(S,     X),
                Arguments.of(SIX,   X)
        );
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Upgrade Lock")
    @MethodSource("upgradeModesProvider")
    public void downgrade(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertUpgradeable(fromMode, toMode, (mode, multiLock) -> { mode.lock(multiLock); return true; });
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Upgrade Lock Interruptibly")
    @MethodSource("upgradeModesProvider")
    public void downgradeInterruptibly(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertUpgradeable(fromMode, toMode, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; });
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Upgrade Lock Try")
    @MethodSource("upgradeModesProvider")
    public void downgradeTry(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertUpgradeable(fromMode, toMode, (mode, multiLock) -> mode.tryLock(multiLock));
    }

    private static void assertUpgradeable(final LockMode from, final LockMode to, final Locker lockFn)
            throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(
                Arrays.asList(new Upgrade(multiLock, from, to, lockFn)),
                LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

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
        private final Locker lockFn;

        private Upgrade(final MultiLock multiLock, final LockMode from, final LockMode to, final Locker lockFn) {
            this.multiLock = multiLock;
            this.from = from;
            this.to = to;
            this.lockFn = lockFn;
        }

        @Override
        public Boolean call() throws InterruptedException {
            lockFn.lock(from, multiLock);
            lockFn.lock(to, multiLock);
            from.unlock(multiLock);
            return true;
        }
    }
}
