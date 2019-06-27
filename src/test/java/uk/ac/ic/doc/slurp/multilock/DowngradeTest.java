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

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Downgrade Lock Try (short timeout)")
    @MethodSource("downgradeModesProvider")
    public void downgradeTryShortTimeout(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertDowngradable(fromMode, toMode, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT / 2, TimeUnit.MILLISECONDS));
    }

    @ParameterizedTest(name = "from {0} to {1}")
    @DisplayName("Downgrade Lock Try (long timeout)")
    @MethodSource("downgradeModesProvider")
    public void downgradeTryLongTimeout(final LockMode fromMode, final LockMode toMode)
            throws InterruptedException, ExecutionException {
        assertDowngradable(fromMode, toMode, (mode, multiLock) -> mode.tryLock(multiLock, LOCK_ACQUISITION_TIMEOUT * 2, TimeUnit.MILLISECONDS));
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
