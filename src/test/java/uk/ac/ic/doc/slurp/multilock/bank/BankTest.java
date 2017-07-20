/*
 * Copyright (c) 2010-2016 Khilan Gudka
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package uk.ac.ic.doc.slurp.multilock.bank;

import java.io.FileNotFoundException;
import java.util.Random;

import uk.ac.ic.doc.slurp.multilock.Lockable;


class Account extends Lockable {
    long balance;
    
    public Account(int bal) {
        balance = bal;
    }
    
    public void withdraw(int amt) {
        if (amt <= balance) {
            balance -= amt;
        }
    }
    
    public void deposit(int amt) {
        balance += amt;
    }
    
}

class Branch extends Lockable {
    Account[] accounts;
    
    public Branch(int numAccounts) {
        accounts = new Account[numAccounts];
        for (int i=0; i<numAccounts; i++) {
            accounts[i] = new Account(100);
        }
    }
    
    public long sumBalances() {
        long sum = 0;
        for (int i=0; i<accounts.length; i++) {
            sum += accounts[i].balance;
        }
        return sum;
    }
    
}

class Bank extends Lockable {
    Branch[] branches;
    
    public Bank(int numBranches, int numAccounts) {
        branches = new Branch[numBranches];
        for (int i=0; i<numBranches; i++) {
            branches[i] = new Branch(numAccounts);
        }
    }
    
    public long sumAll() {
        long sum = 0;
        for (int i=0; i<branches.length; i++) {
            sum += branches[i].sumBalances();
        }
        return sum;
    }
}

public abstract class BankTest {

    public static final int NUM_BRANCHES = 10;
    public static final int NUM_ACCTS_PER_BRANCH = 10;

    public final int numWithdraw, numDeposit, numBranch, numBank;
    
    public BankTest(int nWithdraw, int nDeposit, int nBranch, int nBank) {
        numWithdraw = nWithdraw;
        numDeposit = nDeposit;
        numBranch = nBranch;
        numBank = nBank;
    }
    
    public abstract void sumOneBranch(Bank b, Random r);
    
    public abstract void sumAll(Bank b);
    
    public abstract void withdraw(Bank b, Random r);
    
    public abstract void deposit(Bank b, Random r);
    
    public void runExperiment() throws InterruptedException, FileNotFoundException {
        
        final Bank b = new Bank(NUM_BRANCHES, NUM_ACCTS_PER_BRANCH);
        
        System.out.println("Withdraws: " + numWithdraw);
        System.out.println("Deposits: " + numDeposit);
        System.out.println("Branch: " + numBranch);
        System.out.println("Bank: " + numBank);
        
        for (int ts=1; ts<=16; ts++) {
            final int NUM_THREADS = ts;
            final int NUM_OPS = 1000000;
            
            Thread[] threads = new Thread[NUM_THREADS];
            
            long totalOps = NUM_THREADS*NUM_OPS;
            System.out.println("------------------------");
            System.out.println("Threads: " + NUM_THREADS);
            System.out.println("Ops/Thread: " + NUM_OPS);
            System.out.println("Total ops: " + totalOps);
            System.out.println();
                
            long start = System.currentTimeMillis();
            
//            final AtomicLong withdrawals = new AtomicLong(0),
//                             deposits = new AtomicLong(0),
//                             sumOnes = new AtomicLong(0),
//                             sumAlls = new AtomicLong(0);
            
            for (int i=0; i<NUM_THREADS; i++) {
                Thread t = new Thread() {
                    final Random rnd = new Random();
                    @Override
                    public void run() {
                        for (int j=0; j<NUM_OPS; j++) {
                            int rNum = rnd.nextInt(100);
                            if (rNum < numWithdraw) {
                                withdraw(b, rnd);
                            }
                            else if (rNum < numDeposit) {
                                deposit(b, rnd);                                
                            }
                            else if (rNum < numBranch) {
                                sumOneBranch(b, rnd);
                            }
                            else {
                                sumAll(b);
                            }
                        }
                    }  
                };
                threads[i] = t;
                t.start();
            }
    
            // wait for threads to finish
            for (int i=0; i<NUM_THREADS; i++) {
                threads[i].join();
            }
    
            double took = (System.currentTimeMillis()-start)/1000.0;
//            System.out.println("Withdrawals: " + withdrawals.longValue());
//            System.out.println("Deposits: " + deposits.longValue());
//            System.out.println("Sum ones: " + sumOnes.longValue());
//            System.out.println("Sum alls: " + sumAlls.longValue());
            System.out.println();
            System.out.println("Took (secs): " + String.format("%.2f", took));
            String throughput = String.format("%.2f", (totalOps/took));
            System.out.println("Ops/Sec: " + throughput);
            // output tab-separated result on standard error 
            System.err.println(NUM_THREADS + "," + throughput);
        }

    }
    
}
