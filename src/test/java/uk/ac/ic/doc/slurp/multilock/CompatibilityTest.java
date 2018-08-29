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
 * Tests which check the compatibility of lock modes.
 *
 * For MultiLock the compatibility matrix is as described in the
 * 1975 paper by Gray et al. "Granularity of Locks in a Shared Data Base".
 *
 * |----------------------------------|
 * |           COMPATIBILITY          |
 * |----------------------------------|
 * |     | IS  | IX  | S   | SIX | X  |
 * |----------------------------------|
 * | IS  | YES | YES | YES | YES | NO |
 * | IX  | YES | YES | NO  | NO  | NO |
 * | S   | YES | NO  | YES | NO  | NO |
 * | SIX | YES | NO  | NO  | NO  | NO |
 * | X   | NO  | NO  | NO  | NO  | NO |
 * |----------------------------------|
 *
 * Compatibility is asserted by having a thread t1 acquire
 * a lock mode, and thread t2 acquire another lock mode. Both
 * threads must acquire their lock modes within a defined
 * timeout for the lock modes to be considered compatible.
 *
 * @author Adam Retter <adam@evolvedbinary.com>
 */
public class CompatibilityTest {

    // TODO(AR) this might need to be longer on slower machines...
    private static final long LOCK_ACQUISITION_TIMEOUT = 20;

    static List<Arguments> compatibleModesProvider() {
        return Arrays.asList(
                Arguments.of(IS,    IS,     true),
                Arguments.of(IS,    IX,     true),
                Arguments.of(IS,    S,      true),
                Arguments.of(IS,    SIX,    true),
                Arguments.of(IS,    X,      false),

                Arguments.of(IX,    IS,     true),
                Arguments.of(IX,    IX,     true),
                Arguments.of(IX,    S,      false),
                Arguments.of(IX,    SIX,    false),
                Arguments.of(IX,    X,      false),

                Arguments.of(S,     IS,     true),
                Arguments.of(S,     IX,     false),
                Arguments.of(S,     S,      true),
                Arguments.of(S,     SIX,    false),
                Arguments.of(S,     X,      false),

                Arguments.of(SIX,   IS,     true),
                Arguments.of(SIX,   IX,     false),
                Arguments.of(SIX,   S,      false),
                Arguments.of(SIX,   SIX,    false),
                Arguments.of(SIX,   X,      false),

                Arguments.of(X,     IS,     false),
                Arguments.of(X,     IX,     false),
                Arguments.of(X,     S,      false),
                Arguments.of(X,     SIX,    false),
                Arguments.of(X,     X,      false)
        );
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Compatible Modes")
    @MethodSource("compatibleModesProvider")
    public void compatible(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> { mode.lock(multiLock); return true; });
        } else {
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> { mode.lock(multiLock); return true; });
        }
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Compatible Modes Interruptibly")
    @MethodSource("compatibleModesProvider")
    public void compatibleInterruptibly(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; });
        } else {
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; });
        }
    }

    private static void assertCompatible(final LockMode mode1, final LockMode mode2, final Locker lockFn)
            throws InterruptedException, ExecutionException {
        final List<Future<Boolean>> futures = checkCompatibility(mode1, mode2, lockFn);
        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertTrue(future.get());
        }
    }

    private static void assertNotCompatible(final LockMode mode1, final LockMode mode2, final Locker lockFn)
            throws InterruptedException, ExecutionException {
        final List<Future<Boolean>> futures = checkCompatibility(mode1, mode2, lockFn);
        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());

            assertTrue(future.isCancelled());
        }
    }

    private static List<Future<Boolean>> checkCompatibility(final LockMode mode1, final LockMode mode2,
            final Locker lockFn) throws InterruptedException {
        final MultiLock multiLock = new MultiLock();

        final CountDownLatch latch = new CountDownLatch(2);

        final Callable<Boolean> thread1 = new LockAcquirer(multiLock, mode1, lockFn, latch);
        final Callable<Boolean> thread2 = new LockAcquirer(multiLock, mode2, lockFn, latch);

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        return executorService.invokeAll(Arrays.asList(thread1, thread2), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private static class LockAcquirer implements Callable<Boolean> {
        private final MultiLock multiLock;
        private final LockMode lockMode;
        private final Locker lockFn;
        private final CountDownLatch latch;

        public LockAcquirer(final MultiLock multiLock, final LockMode lockMode, final Locker lockFn,
                final CountDownLatch latch) {
            this.multiLock = multiLock;
            this.lockMode = lockMode;
            this.lockFn = lockFn;
            this.latch = latch;
        }

        @Override
        public Boolean call() throws Exception {
            lockFn.lock(lockMode, multiLock);

            latch.countDown();
            latch.await();

            return true;
        }
    }
}
