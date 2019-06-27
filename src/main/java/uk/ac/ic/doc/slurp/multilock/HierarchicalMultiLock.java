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

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

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
    public void readLock() {
        if (parent != null) {
            parent.intentionReadLock();
        }
        super.readLock();
    }

    @Override
    public boolean tryReadLock() {
        if (parent != null) {
            if(!parent.tryIntentionReadLock()) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryReadLock();
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionRead();
            }
        }
        return locked;
    }

    /**
     * @param timeout the time to wait for acquiring the locks. The actual time can be 2* this, as the
     *     timeout is used twice, once for the parent and once for the node.
     * @param unit the time unit of the timeout argument
     */
    @Override
    public boolean tryReadLock(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (parent != null) {
            if(!parent.tryIntentionReadLock(timeout, unit)) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryReadLock(timeout, unit);
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionRead();
            }
            throw e;
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionRead();
            }
        }
        return locked;
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
    public void writeLock() {
        if (parent != null) {
            parent.intentionWriteLock();
        }
        super.writeLock();
    }

    @Override
    public boolean tryWriteLock() {
        if (parent != null) {
            if(!parent.tryIntentionWriteLock()) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryWriteLock();
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionWrite();
            }
        }
        return locked;
    }

    /**
     * @param timeout the time to wait for acquiring the locks. The actual time can be 2* this, as the
     *     timeout is used twice, once for the parent and once for the node.
     * @param unit the time unit of the timeout argument
     */
    @Override
    public boolean tryWriteLock(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (parent != null) {
            if(!parent.tryIntentionWriteLock(timeout, unit)) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryWriteLock(timeout, unit);
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionWrite();
            }
            throw e;
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionWrite();
            }
        }
        return locked;
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
    public void intentionReadLock() {
        if (parent != null) {
            parent.intentionReadLock();
        }
        super.intentionReadLock();
    }

    @Override
    public boolean tryIntentionReadLock() {
        if (parent != null) {
            if(!parent.tryIntentionReadLock()) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryIntentionReadLock();
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionRead();
            }
        }
        return locked;
    }

    /**
     * @param timeout the time to wait for acquiring the locks. The actual time can be 2* this, as the
     *     timeout is used twice, once for the parent and once for the node.
     * @param unit the time unit of the timeout argument
     */
    @Override
    public boolean tryIntentionReadLock(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (parent != null) {
            if(!parent.tryIntentionReadLock(timeout, unit)) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryIntentionReadLock(timeout, unit);
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionRead();
            }
            throw e;
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionRead();
            }
        }
        return locked;
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
    public void intentionWriteLock() {
        if (parent != null) {
            parent.intentionWriteLock();
        }
        super.intentionWriteLock();
    }

    @Override
    public boolean tryIntentionWriteLock() {
        if (parent != null) {
            if(!parent.tryIntentionWriteLock()) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryIntentionWriteLock();
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionWrite();
            }
        }
        return locked;
    }

    /**
     * @param timeout the time to wait for acquiring the locks. The actual time can be 2* this, as the
     *     timeout is used twice, once for the parent and once for the node.
     * @param unit the time unit of the timeout argument
     */
    @Override
    public boolean tryIntentionWriteLock(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (parent != null) {
            if(!parent.tryIntentionWriteLock(timeout, unit)) {
                return false;
            }
        }

        boolean locked = false;
        try {
            locked = super.tryIntentionWriteLock(timeout, unit);
        } catch (final InterruptedException e) {
            if (parent != null) {
                parent.unlockIntentionWrite();
            }
            throw e;
        } finally {
            if (!locked && parent != null) {
                parent.unlockIntentionWrite();
            }
        }
        return locked;
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
