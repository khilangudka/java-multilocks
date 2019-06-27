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
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ic.doc.slurp.multilock.Constants.LOCK_ACQUISITION_TIMEOUT;

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
    private static final long RENTER_LOCK_ACQUISITIONS_TIMEOUT = LOCK_ACQUISITION_TIMEOUT * REENTER_COUNT;

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reentrant")
    @EnumSource(value = LockMode.class)
    public void reentrant(final LockMode lockMode) throws InterruptedException, ExecutionException {
        assertReentrant(lockMode, (mode, multiLock) -> { mode.lock(multiLock); return true; });
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reentrant Interruptibly")
    @EnumSource(value = LockMode.class)
    public void reentrantInterruptibly(final LockMode lockMode) throws InterruptedException, ExecutionException {
        assertReentrant(lockMode, (mode, multiLock) -> { mode.lockInterruptibly(multiLock); return true; });
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reentrant Try")
    @EnumSource(value = LockMode.class)
    public void reentrantTry(final LockMode lockMode) throws InterruptedException, ExecutionException {
        assertReentrant(lockMode, (mode, multiLock) -> { mode.tryLock(multiLock); return true; });
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reentrant Try (short timeout)")
    @EnumSource(value = LockMode.class)
    public void reentrantTryShortTimeout(final LockMode lockMode) throws InterruptedException, ExecutionException {
        assertReentrant(lockMode, (mode, multiLock) -> { mode.tryLock(multiLock, RENTER_LOCK_ACQUISITIONS_TIMEOUT / 2, TimeUnit.MILLISECONDS); return true; });
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Reentrant Try (long timeout)")
    @EnumSource(value = LockMode.class)
    public void reentrantTryLongTimeout(final LockMode lockMode) throws InterruptedException, ExecutionException {
        assertReentrant(lockMode, (mode, multiLock) -> { mode.tryLock(multiLock, RENTER_LOCK_ACQUISITIONS_TIMEOUT * 2, TimeUnit.MILLISECONDS); return true; });
    }

    private static void assertReentrant(final LockMode lockMode, final Locker lockFn)
            throws InterruptedException, ExecutionException {
        final MultiLock multiLock = new MultiLock();

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Integer>> futures = executorService.invokeAll(
                Arrays.asList(new Reenter(multiLock, lockMode, lockFn, REENTER_COUNT)),
                RENTER_LOCK_ACQUISITIONS_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Integer> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertEquals(REENTER_COUNT, (int)future.get());
        }
    }

    private static class Reenter implements Callable<Integer> {
        private final MultiLock multiLock;
        private final LockMode lockMode;
        private final Locker lockFn;
        private final int count;

        private Reenter(final MultiLock multiLock, final LockMode lockMode, final Locker lockFn, final int count) {
            this.multiLock = multiLock;
            this.lockMode = lockMode;
            this.lockFn = lockFn;
            this.count = count;
        }

        @Override
        public Integer call() throws InterruptedException {
            int success = 0;
            for (int i = 0; i < count; i++) {
                lockFn.lock(lockMode, multiLock);
                success++;
            }
            return success;
        }
    }
}
