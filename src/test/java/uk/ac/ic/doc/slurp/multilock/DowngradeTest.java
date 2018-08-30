package uk.ac.ic.doc.slurp.multilock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    // TODO(AR) this might need to be longer on slower machines...
    private static final long LOCK_ACQUISITION_TIMEOUT = 40;

    static List<Arguments> downgradeModesProvider() {
        return Arrays.asList(
                Arguments.of(IX,    IS),
                Arguments.of(S,     IS),
                Arguments.of(SIX,   IX),
                Arguments.of(SIX,   S),
                Arguments.of(SIX,   IS),
                Arguments.of(X,     SIX),
                Arguments.of(X,     IX),
                Arguments.of(X,     S),
                Arguments.of(X,     IS)
        );
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Downgrade Lock")
    @MethodSource("downgradeModesProvider")
    public void downgrade(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertDowngradable(fromMode, toMode, (mode, multiLock) -> { mode.lock(multiLock); return true; });
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Downgrade Lock Interruptibly")
    @MethodSource("downgradeModesProvider")
    public void downgradeInterruptibly(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertDowngradable(fromMode, toMode, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; });
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Downgrade Lock Try")
    @MethodSource("downgradeModesProvider")
    public void downgradeTry(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertDowngradable(fromMode, toMode, (mode, multiLock) -> mode.tryLock(multiLock));
    }

    private static void assertDowngradable(final LockMode from, final LockMode to, final Locker lockFn)
            throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(
                Arrays.asList(new Downgrade(multiLock, from, to, lockFn)),
                LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

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
        private final Locker lockFn;

        private Downgrade(final MultiLock multiLock, final LockMode from, final LockMode to, final Locker lockFn) {
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
