package uk.ac.ic.doc.slurp.multilock;

import java.util.concurrent.TimeUnit;

/**
 * An Enumeration of the MultiLock modes.
 */
public enum LockMode {
    /**
     * Intention Shared
     */
    IS,

    /**
     * Intention Exclusive
     */
    IX,

    /**
     * Shared
     */
    S,

    /**
     * Shared Intention Exclusive
     */
    SIX,

    /**
     * Exclusive
     */
    X;

    /**
     * Locks the MultiLock with this mode.
     *
     * @param multiLock the MultiLock object.
     */
    public void lock(final MultiLock multiLock) {
        lock(multiLock, this);
    }

    /**
     * Attempts to lock the MultiLock with this mode.
     *
     * @param multiLock the MultiLock object.
     *
     * @return true if the lock was acquired, false otherwise.
     */
    public boolean tryLock(final MultiLock multiLock) {
        return tryLock(multiLock, this);
    }

    /**
     * Attempts to lock the MultiLock with this mode.
     *
     * @param multiLock the MultiLock object.
     * @param timeout the time to wait for acquiring the lock.
     * @param unit the time unit of the timeout argument.
     *
     * @return true if the lock was acquired, false otherwise.
     *
     * @throws InterruptedException if the thread was interrupted
     */
    public boolean tryLock(final MultiLock multiLock, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        return tryLock(multiLock, this, timeout, unit);
    }

    /**
     * Locks the MultiLock with this mode, aborting if interrupted.
     *
     * @param multiLock the MultiLock object.
     *
     * @throws InterruptedException if the thread was interrupted
     */
    public void lockInterruptibly(final MultiLock multiLock) throws InterruptedException {
        lockInterruptibly(multiLock, this);
    }

    /**
     * Unlocks the MultiLock with this mode.
     *
     * @param multiLock the MultiLock object.
     */
    public void unlock(final MultiLock multiLock) {
        unlock(multiLock, this);
    }

    /**
     * Locks the MultiLock with the provided mode.
     *
     * @param multiLock the MultiLock object.
     * @param lockMode the mode to lock the MultiLock.
     *
     * @return true if the lock succeeded, false otherwise.
     *
     * @throws IllegalArgumentException if an unknown mode is provided.
     */
    public static void lock(final MultiLock multiLock, final LockMode lockMode) {
        switch (lockMode) {
            case IS:
                multiLock.intentionReadLock();
                break;

            case IX:
                multiLock.intentionWriteLock();
                break;

            case S:
                multiLock.readLock();
                break;

            case SIX:
                multiLock.readLock();
                multiLock.intentionWriteLock();
                break;

            case X:
                multiLock.writeLock();
                break;

            default:
                throw new IllegalArgumentException("Unknown lock mode: " + lockMode);
        }
    }

    /**
     * Attempts to lock the MultiLock with the provided mode.
     *
     * @param multiLock the MultiLock object.
     * @param lockMode the mode to lock the MultiLock.
     *
     * @return true if the lock was acquired.
     *
     * @throws IllegalArgumentException if an unknown mode is provided.
     * @throws InterruptedException if the thread was interrupted
     */
    public static boolean tryLock(final MultiLock multiLock, final LockMode lockMode) {
        switch (lockMode) {
            case IS:
                return multiLock.tryIntentionReadLock();

            case IX:
                return multiLock.tryIntentionWriteLock();

            case S:
                return multiLock.tryReadLock();

            case SIX:
                if (!multiLock.tryReadLock()) {
                    return false;
                }
                if (!multiLock.tryIntentionWriteLock()) {
                    multiLock.unlockRead();
                    return false;
                }
                return true;

            case X:
                return multiLock.tryWriteLock();

            default:
                throw new IllegalArgumentException("Unknown lock mode: " + lockMode);
        }
    }

    /**
     * Attempts to lock the MultiLock with the provided mode.
     *
     * @param multiLock the MultiLock object.
     * @param lockMode the mode to lock the MultiLock.
     * @param timeout the time to wait for acquiring the lock.
     * @param unit the time unit of the timeout argument.
     *
     * @return true if the lock was acquired.
     *
     * @throws IllegalArgumentException if an unknown mode is provided.
     * @throws InterruptedException if the thread was interrupted
     */
    public static boolean tryLock(final MultiLock multiLock, final LockMode lockMode, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        switch (lockMode) {
            case IS:
                return multiLock.tryIntentionReadLock(timeout, unit);

            case IX:
                return multiLock.tryIntentionWriteLock(timeout, unit);

            case S:
                return multiLock.tryReadLock(timeout, unit);

            case SIX:
                if (!multiLock.tryReadLock(timeout, unit)) {
                    return false;
                }
                try {
                    if (!multiLock.tryIntentionWriteLock(timeout, unit)) {
                        multiLock.unlockRead();
                        return false;
                    }
                } catch (final InterruptedException e) {
                    multiLock.unlockRead();
                    throw e;
                }
                return true;

            case X:
                return multiLock.tryWriteLock(timeout, unit);

            default:
                throw new IllegalArgumentException("Unknown lock mode: " + lockMode);
        }
    }

    /**
     * Locks the MultiLock with the provided mode, aborting if interrupted.
     *
     * @param multiLock the MultiLock object.
     * @param lockMode the mode to lock the MultiLock.
     *
     * @throws IllegalArgumentException if an unknown mode is provided.
     * @throws InterruptedException if the thread was interrupted
     */
    public static void lockInterruptibly(final MultiLock multiLock, final LockMode lockMode) throws InterruptedException {
        switch (lockMode) {
            case IS:
                multiLock.intentionReadLockInterruptibly();
                break;

            case IX:
                multiLock.intentionWriteLockInterruptibly();
                break;

            case S:
                multiLock.readLockInterruptibly();
                break;

            case SIX:
                multiLock.readLockInterruptibly();
                try {
                    multiLock.intentionWriteLockInterruptibly();
                } catch (final InterruptedException e) {
                    multiLock.unlockRead();
                    throw e;
                }
                break;

            case X:
                multiLock.writeLockInterruptibly();
                break;

            default:
                throw new IllegalArgumentException("Unknown lock mode: " + lockMode);
        }
    }

    /**
     * Unlocks the MultiLock with the provided mode.
     *
     * @param multiLock the MultiLock object.
     * @param lockMode the mode to unlock the MultiLock.
     *
     * @throws IllegalArgumentException if an unknown mode is provided.
     */
    public static void unlock(final MultiLock multiLock, final LockMode lockMode) {
        switch (lockMode) {
            case IS:
                multiLock.unlockIntentionRead();
                break;

            case IX:
                multiLock.unlockIntentionWrite();
                break;

            case S:
                multiLock.unlockRead();
                break;

            case SIX:
                multiLock.unlockIntentionWrite();
                multiLock.unlockRead();
                break;

            case X:
                multiLock.unlockWrite();
                break;

            default:
                throw new IllegalArgumentException("Unknown lock mode: " + lockMode);
        }
    }
}
