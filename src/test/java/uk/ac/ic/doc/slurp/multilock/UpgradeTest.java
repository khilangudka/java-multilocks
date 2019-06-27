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

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Upgrade Lock Try (short timeout)")
    @MethodSource("upgradeModesProvider")
    public void downgradeTryShortTimeout(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertUpgradeable(fromMode, toMode, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT / 2, TimeUnit.MILLISECONDS));
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Upgrade Lock Try (long timeout)")
    @MethodSource("upgradeModesProvider")
    public void downgradeTryLongTimeout(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertUpgradeable(fromMode, toMode, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT * 2, TimeUnit.MILLISECONDS));
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
