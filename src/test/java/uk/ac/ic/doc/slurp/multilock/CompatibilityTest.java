/**
 * Copyright © 2010, Khilan Gudka
 * All rights reserved.
 *
 * Modifications and additions from the original source code at
 * https://github.com/kgudka/java-multilocks are
 * Copyright © 2017, Evolved Binary
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ic.doc.slurp.multilock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ic.doc.slurp.multilock.Constants.LOCK_ACQUISITION_TIMEOUT;
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

    @ParameterizedTest(name = "{0} and {1}")
    @DisplayName("Compatible Modes")
    @MethodSource("compatibleModesProvider")
    public void compatible(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> { mode.lock(multiLock); return true; });
        } else {
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> { mode.lock(multiLock); return true; }, true);
        }
    }

    @ParameterizedTest(name = "{0} and {1}")
    @DisplayName("Compatible Modes Interruptibly")
    @MethodSource("compatibleModesProvider")
    public void compatibleInterruptibly(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; });
        } else {
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; }, true);
        }
    }

    @ParameterizedTest(name = "{0} and {1}")
    @DisplayName("Compatible Modes Try")
    @MethodSource("compatibleModesProvider")
    public void compatibleTry(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> mode.tryLock(multiLock));
        } else {
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> mode.tryLock(multiLock), false);
        }
    }

    @ParameterizedTest(name = "{0} and {1}")
    @DisplayName("Compatible Modes Try (short timeout)")
    @MethodSource("compatibleModesProvider")
    public void compatibleTryShortTimeout(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT / 2, TimeUnit.MILLISECONDS));
        } else {
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT / 2, TimeUnit.MILLISECONDS), false);
        }
    }

    @ParameterizedTest(name = "{0} and {1}")
    @DisplayName("Compatible Modes Try (long timeout)")
    @MethodSource("compatibleModesProvider")
    public void compatibleTryLongTimeout(final LockMode mode1, final LockMode mode2, final boolean compatible)
            throws InterruptedException, ExecutionException {
        if (compatible) {
            assertCompatible(mode1, mode2, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT * 2, TimeUnit.MILLISECONDS));
        } else {
            // NOTE: we set the blockingAcquisition=true parameter, as the timeout is greater than the ExecutorService's LOCK_ACQUISITION_TIMEOUT
            assertNotCompatible(mode1, mode2, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT * 2, TimeUnit.MILLISECONDS), true);
        }
    }

    /**
     * Assert that two lock modes are compatible.
     *
     * @param mode1 the first lock mode
     * @param mode2 the second lock mode
     * @param lockFn the function for acquiring a lock
     */
    private static void assertCompatible(final LockMode mode1, final LockMode mode2, final Locker lockFn)
            throws InterruptedException, ExecutionException {
        final List<Future<Boolean>> futures = checkCompatibility(mode1, mode2, lockFn);
        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertTrue(future.get());
        }
    }

    /**
     * Assert that two lock modes are not compatible.
     *
     * @param mode1 the first lock mode
     * @param mode2 the second lock mode
     * @param lockFn the function for acquiring a lock
     * @param blockingAcquisition true if the {@code lockFn} makes lock acquisition calls which block
     *     until the lock is granted, false otherwise.
     */
    private static void assertNotCompatible(final LockMode mode1, final LockMode mode2, final Locker lockFn,
            final boolean blockingAcquisition)
            throws InterruptedException, ExecutionException {
        final List<Future<Boolean>> futures = checkCompatibility(mode1, mode2, lockFn);

        Boolean locked = null;

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());

            if (!future.isCancelled()) {
                if (locked == null) {
                    locked = future.get();
                } else {
                    locked &= future.get();
                }
            }
        }

        /*
         * `locked` will be null if all futures were cancelled,
         * this is because each of our LockAcquirer(s) only complete
         * if all acquisitions succeed. When using blocking acquisitions
         * if one acquisition fails, no tasks complete, so all futures
         * end up marked as cancelled. See the latch in LockAcquirer#call()
         */
        assertTrue((blockingAcquisition && locked == null) || (!blockingAcquisition && !locked));
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
            final boolean locked = lockFn.lock(lockMode, multiLock);

            latch.countDown();
            latch.await();

            return locked;
        }
    }
}
