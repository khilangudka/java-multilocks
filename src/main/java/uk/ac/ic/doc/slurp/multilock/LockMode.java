package uk.ac.ic.doc.slurp.multilock;

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
