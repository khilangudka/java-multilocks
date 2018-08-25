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
    public boolean lockRead() {
        if (parent != null) {
            parent.lockIntentionRead();
        }
        return super.lockRead();
    }

    @Override
    public boolean lockWrite() {
        if (parent != null) {
            parent.lockIntentionWrite();
        }
        return super.lockWrite();
    }

    @Override
    public boolean lockIntentionRead() {
        if (parent != null) {
            parent.lockIntentionRead();
        }
        return super.lockIntentionRead();
    }

    @Override
    public boolean lockIntentionWrite() {
        if (parent != null) {
            parent.lockIntentionWrite();
        }
        return super.lockIntentionWrite();
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
