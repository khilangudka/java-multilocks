package uk.ac.ic.doc.slurp.multilock;

import javax.annotation.Nullable;

public class HierarchicalMultiLock extends MultiLock {

    private final MultiLock parent;

    /**
     * Constructs a MultiLock where the lock
     * is within a lock hierarchy.
     *
     * @param parent the parent lock, or null if this lock
     *   should be the root of the hierarchy.
     */
    public HierarchicalMultiLock(@Nullable final MultiLock parent) {
        super();
        this.parent = parent;
    }

    @Override
    public boolean readLock() {
        if (parent != null) {
            parent.intentionReadLock();
        }
        return super.readLock();
    }

    @Override
    public void readLockInterruptibly() throws InterruptedException {
        if (parent != null) {
            parent.intentionReadLockInterruptibly();
        }

        try {
            super.readLockInterruptibly();
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionRead();
            }
            throw e;
        }
    }

    @Override
    public boolean writeLock() {
        if (parent != null) {
            parent.intentionWriteLock();
        }
        return super.writeLock();
    }

    @Override
    public void writeLockInterruptibly() throws InterruptedException {
        if (parent != null) {
            parent.intentionWriteLockInterruptibly();
        }

        try {
            super.writeLockInterruptibly();
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionWrite();
            }
            throw e;
        }
    }

    @Override
    public boolean intentionReadLock() {
        if (parent != null) {
            parent.intentionReadLock();
        }
        return super.intentionReadLock();
    }

    @Override
    public void intentionReadLockInterruptibly() throws InterruptedException {
        if (parent != null) {
            parent.intentionReadLockInterruptibly();
        }

        try {
            super.intentionReadLockInterruptibly();
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionRead();
            }
            throw e;
        }
    }

    @Override
    public boolean intentionWriteLock() {
        if (parent != null) {
            parent.intentionWriteLock();
        }
        return super.intentionWriteLock();
    }

    @Override
    public void intentionWriteLockInterruptibly() throws InterruptedException {
        if (parent != null) {
            parent.intentionWriteLockInterruptibly();
        }

        try {
            super.intentionWriteLockInterruptibly();
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionWrite();
            }
            throw e;
        }
    }

    @Override
    public void unlockRead() {
        super.unlockRead();
        if (parent != null) {
            parent.unlockIntentionRead();
        }
    }

    @Override
    public void unlockWrite() {
        super.unlockWrite();
        if (parent != null) {
            parent.unlockIntentionWrite();
        }
    }

    @Override
    public void unlockIntentionRead() {
        super.unlockIntentionRead();
        if (parent != null) {
            parent.unlockIntentionRead();
        }
    }

    @Override
    public void unlockIntentionWrite() {
        super.unlockIntentionWrite();
        if (parent != null) {
            parent.unlockIntentionWrite();
        }
    }
}
