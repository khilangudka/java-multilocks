package uk.ac.ic.doc.slurp.multilock;

@FunctionalInterface
public interface Locker {
    boolean lock(final LockMode lockMode, final MultiLock multiLock) throws InterruptedException;
}
