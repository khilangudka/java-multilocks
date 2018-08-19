package uk.ac.ic.doc.slurp.multilock;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.ic.doc.slurp.multilock.LockMode.*;

/**
 * Uses a lock hierarchy of two levels, root and child
 * to test what lock acquisitions may be made on the child
 * in the presence of locks on the root.
 *
 * Functions named like {@code t1_root_IS_t2_child_IX}
 * should be read as, Test where:
 *     Thread 1 has IS lock on root,
 *     and Thread 2 will attempt IX lock on child.
 *
 * There are two types of tests here:
 *
 * 1. Single threaded tests which check what modes the accessor
 *     thread is allowed to take on the child after locking the root.
 *
 * 2. Bi-threaded tests where thread t1 locks the root, and then
 *     thread t2 tries to lock the child.
 *
 * @author Adam Retter <adam@evolvedbinary.com>
 */
public class HierarchyTest {

    private static final long LOCK_ACQUISITION_TIMEOUT = 20;        // TODO(AR) this might need to be longer on slower machines...

    /* single threaded tests below */

    @Test
    public void t1_root_IS_t1_child_IS() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IS, IS);
    }

    // TODO - page 7 of Gray's paper - Granularity of Locks in a Shared Data Base
    @Test
    public void t1_root_IS_t1_child_IX() throws ExecutionException, InterruptedException {
        assertLockRootThenNotChild(IS, IX);
    }

    @Test
    public void t1_root_IS_t1_child_S() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IS, S);
    }

    // TODO - page 7 of Gray's paper - Granularity of Locks in a Shared Data Base
    @Test
    public void t1_root_IS_t1_child_SIX() throws ExecutionException, InterruptedException {
        assertLockRootThenNotChild(IS, SIX);
    }

    // TODO - page 7 of Gray's paper - Granularity of Locks in a Shared Data Base
    @Test
    public void t1_root_IS_t1_child_X() throws ExecutionException, InterruptedException {
        assertLockRootThenNotChild(IS, X);
    }

    @Test
    public void t1_root_IX_t1_child_IS() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IX, IS);
    }

    @Test
    public void t1_root_IX_t1_child_IX() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IX, IX);
    }

    @Test
    public void t1_root_IX_t1_child_S() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IX, S);
    }

    @Test
    public void t1_root_IX_t1_child_SIX() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IX, SIX);
    }

    @Test
    public void t1_root_IX_t1_child_X() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(IX, X);
    }

    //TODO(AR) need tests for t1_root_S_child... -- I think it's likely that S on root should only be able to get S on child

    // TODO - page 7 of Gray's paper - Granularity of Locks in a Shared Data Base
    @Test
    public void t1_root_SIX_t1_child_IS() throws ExecutionException, InterruptedException {
        assertLockRootThenNotChild(SIX, IS);
    }

    @Test
    public void t1_root_SIX_t1_child_IX() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(SIX, IX);
    }

    // TODO - page 7 of Gray's paper - Granularity of Locks in a Shared Data Base
    @Test
    public void t1_root_SIX_t1_child_S() throws ExecutionException, InterruptedException {
        assertLockRootThenNotChild(SIX, S);
    }

    @Test
    public void t1_root_SIX_t1_child_SIX() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(SIX, SIX);
    }

    @Test
    public void t1_root_SIX_t1_child_X() throws ExecutionException, InterruptedException {
        assertLockRootThenChild(SIX, X);
    }

    //TODO(AR) need tests for t1_root_X_child... -- I think it's likely that X on root should only be able to get X on child


    /* bi-threaded tests below */

    @Test
    public void t1_root_IS_t2_child_IS() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IS.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, IS);

    }

    @Test
    public void t1_root_IS_t2_child_IX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IS.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, IX);
    }

    @Test
    public void t1_root_IS_t2_child_S() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IS.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, S);
    }

    @Test
    public void t1_root_IS_t2_child_SIX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IS.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, SIX);
    }

    @Test
    public void t1_root_IS_t2_child_X() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IS.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, X);
    }

    @Test
    public void t1_root_IX_t2_child_IS() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, IS);

    }

    @Test
    public void t1_root_IX_t2_child_IX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, IX);
    }

    @Test
    public void t1_root_IX_t2_child_S() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, S);
    }

    @Test
    public void t1_root_IX_t2_child_SIX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, SIX);
    }

    @Test
    public void t1_root_IX_t2_child_X() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(IX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, SIX);
    }

    @Test
    public void t1_root_S_t2_child_IS() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(S.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, IS);

    }

    @Test
    public void t1_root_S_t2_child_IX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(S.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds S on root, other thread attempts IX on child (which attempts IX on root). S is not compatible with IX
        assertOtherThreadNotLockableChild(child, IX);
    }

    @Test
    public void t1_root_S_t2_child_S() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(S.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, S);
    }

    @Test
    public void t1_root_S_t2_child_SIX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(S.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds S on root, other thread attempts SIX on child (which attempts S+IX on root). S is not compatible with IX
        assertOtherThreadNotLockableChild(child, SIX);
    }

    @Test
    public void t1_root_S_t2_child_X() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(S.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds S on root, other thread attempts X on child (which attempts IX on root). S is not compatible with IX
        assertOtherThreadNotLockableChild(child, X);
    }

    @Test
    public void t1_root_SIX_t2_child_IS() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(SIX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, IS);

    }

    @Test
    public void t1_root_SIX_t2_child_IX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(SIX.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds S+IX on root, other thread attempts IX on child (which attempts IX on root). S is not compatible with IX
        assertOtherThreadNotLockableChild(child, IX);
    }

    @Test
    public void t1_root_SIX_t2_child_S() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(SIX.lock(root));

        final MultiLock child = new MultiLock(root);
        assertOtherThreadLockableChild(child, S);
    }

    @Test
    public void t1_root_SIX_t2_child_SIX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(SIX.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds S+IX on root, other thread attempts SIX on child (which attempts IS+IX on root). S is not compatible with IX
        assertOtherThreadNotLockableChild(child, SIX);
    }

    @Test
    public void t1_root_SIX_t2_child_X() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(SIX.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds S+IX on root, other thread attempts X on child (which attempts IX on root). S is not compatible with IX
        assertOtherThreadNotLockableChild(child, X);
    }

    @Test
    public void t1_root_X_t2_child_IS() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(X.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds X on root, other thread attempts IS on child (which attempts IS on root). X is not compatible with IS
        assertOtherThreadNotLockableChild(child, IS);

    }

    @Test
    public void t1_root_X_t2_child_IX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(X.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds X on root, other thread attempts IX on child (which attempts IX on root). X is not compatible with IX
        assertOtherThreadNotLockableChild(child, IX);
    }

    @Test
    public void t1_root_X_t2_child_S() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(X.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds X on root, other thread attempts S on child (which attempts IS on root). X is not compatible with IS
        assertOtherThreadNotLockableChild(child, S);
    }

    @Test
    public void t1_root_X_t2_child_SIX() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(X.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds X on root, other thread attempts SIX on child (which attempts IS+IX on root). X is not compatible with IS or IX
        assertOtherThreadNotLockableChild(child, SIX);
    }

    @Test
    public void t1_root_X_t2_child_X() throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        assertTrue(X.lock(root));

        final MultiLock child = new MultiLock(root);
        // notLockable = test thread holds X on root, other thread attempts X on child (which attempts IX on root). X is not compatible with IX
        assertOtherThreadNotLockableChild(child, X);
    }

    private static void assertLockRootThenChild(final LockMode rootMode, final LockMode childMode) throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        final MultiLock child = new MultiLock(root);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(Arrays.asList(new RootAndChildLockAcquirer(root, rootMode, child, childMode)), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());

            assertFalse(future.isCancelled());

            assertTrue(future.get());
        }
    }

    private static void assertLockRootThenNotChild(final LockMode rootMode, final LockMode childMode) throws InterruptedException, ExecutionException {
        final MultiLock root = new MultiLock(null);
        final MultiLock child = new MultiLock(root);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(Arrays.asList(new RootAndChildLockAcquirer(root, rootMode, child, childMode)), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());

            assertTrue(future.isCancelled());
        }
    }

    private static void assertOtherThreadLockableChild(final MultiLock child, final LockMode childMode) throws InterruptedException, ExecutionException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(Arrays.asList(new ChildLockAcquirer(child, childMode)), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

            assertTrue(future.get());
        }
    }

    private static void assertOtherThreadNotLockableChild(final MultiLock child, final LockMode childMode) throws InterruptedException, ExecutionException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final List<Future<Boolean>> futures = executorService.invokeAll(Arrays.asList(new ChildLockAcquirer(child, childMode)), LOCK_ACQUISITION_TIMEOUT, TimeUnit.MILLISECONDS);

        for (final Future<Boolean> future : futures) {
            assertTrue(future.isDone());

            assertTrue(future.isCancelled());
        }
    }

    private static class RootAndChildLockAcquirer implements Callable<Boolean> {
        private final MultiLock root;
        private final LockMode rootLockMode;
        private final MultiLock child;
        private final LockMode childLockMode;

        public RootAndChildLockAcquirer(final MultiLock root, final LockMode rootLockMode, final MultiLock child, final LockMode childLockMode) {
            this.root = root;
            this.rootLockMode = rootLockMode;
            this.child = child;
            this.childLockMode = childLockMode;
        }

        @Override
        public Boolean call() {
            if (rootLockMode.lock(root)) {
                // NOTE: we intentionally don't combine the two boolean expressions to make debugging the cause of deadlocks simpler
                return childLockMode.lock(child);
            }
            return false;
        }
    }

    private static class ChildLockAcquirer implements Callable<Boolean> {
        private final MultiLock child;
        private final LockMode lockMode;

        public ChildLockAcquirer(final MultiLock child, final LockMode lockMode) {
            this.child = child;
            this.lockMode = lockMode;
        }

        @Override
        public Boolean call() {
            return lockMode.lock(child);
        }
    }
}
