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
     *
     * @return true if the lock succeeded, false otherwise.
     */
    public boolean lock(final MultiLock multiLock) {
        return lock(multiLock, this);
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
    public static boolean lock(final MultiLock multiLock, final LockMode lockMode) {
        final boolean lockResult;
        switch (lockMode) {
            case IS:
                lockResult = multiLock.lockIntentionRead();
                break;

            case IX:
                lockResult = multiLock.lockIntentionWrite();
                break;

            case S:
                lockResult = multiLock.lockRead();
                break;

            case SIX:
                lockResult = multiLock.lockRead() && multiLock.lockIntentionWrite();
                break;

            case X:
                lockResult = multiLock.lockWrite();
                break;

            default:
                throw new IllegalArgumentException("Unknown lock mode: " + lockMode);
        }

        return lockResult;
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
