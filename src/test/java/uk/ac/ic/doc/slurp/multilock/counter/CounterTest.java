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
package uk.ac.ic.doc.slurp.multilock.counter;

import uk.ac.ic.doc.slurp.multilock.Lockable;

class Counter extends Lockable {
    long value = 0;
    public void inc() {
        value++;
    }
}

public abstract class CounterTest {

    public abstract void inc(Counter c);
    
    /**
     * @param args
     * @throws InterruptedException 
     */
    public void runExperiment() throws InterruptedException {
        
        for (int ts=1; ts<=16; ts++) {
            final int nThreads = ts;
            final int nIncs = 1000000;
            final Counter c = new Counter();
            
            System.out.println("Threads: " + nThreads);
            System.out.println("Increments: " + nIncs);
            System.out.println();
            
            Thread[] threads = new Thread[nThreads];
    
            long start = System.currentTimeMillis();
            for (int i=0; i<nThreads; i++) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        for (int j=0; j<nIncs; j++) {
                            inc(c);
                        }
                    }
                };
                threads[i] = t;
                t.start();
            }
            
            for (int i=0; i<nThreads; i++) {
                threads[i].join();
            }
            double took = (System.currentTimeMillis() - start)/1000.0; // time in seconds
            long expected = nThreads*nIncs;
            double throughput = expected/took;
            System.out.println("Expected uk.ac.ic.doc.slurp.multilock.counter value: " + expected);
            System.out.println("Actual uk.ac.ic.doc.slurp.multilock.counter value: " + c.value);
            System.out.println("Running time (secs): " + String.format("%.2f", took));
            String throughputStr = String.format("%.2f", throughput);
            System.out.println("incs/sec: " + throughputStr);
            System.err.println(nThreads + "," + throughputStr);
        }
    }

}
