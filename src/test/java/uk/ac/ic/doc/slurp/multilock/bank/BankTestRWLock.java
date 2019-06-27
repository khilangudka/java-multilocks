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
package uk.ac.ic.doc.slurp.multilock.bank;

import java.io.FileNotFoundException;
import java.util.Random;

public class BankTestRWLock extends BankTest {

    public BankTestRWLock(int nWithdraw, int nDeposit, int nBranch, int nBank) {
        super(nWithdraw, nDeposit, nBranch, nBank);
    }

    public void withdraw(Bank b, Random r) {
        int branchId = r.nextInt(NUM_BRANCHES);
        int acctId = r.nextInt(NUM_ACCTS_PER_BRANCH);
        int amt = r.nextInt(60);

        b.rlock.lock();
        Branch branch = b.branches[branchId];
        branch.rlock.lock();
        Account acct = branch.accounts[acctId];
        acct.wlock.lock();
        
        acct.withdraw(amt);

        acct.wlock.unlock();
        branch.rlock.unlock();
        b.rlock.unlock();
    }
    
    public void deposit(Bank b, Random r) {
        int branchId = r.nextInt(NUM_BRANCHES);
        int acctId = r.nextInt(NUM_ACCTS_PER_BRANCH);
        int amt = r.nextInt(60);

        b.rlock.lock();
        Branch branch = b.branches[branchId];
        branch.rlock.lock();
        Account acct = branch.accounts[acctId];
        acct.wlock.lock();
        
        acct.deposit(amt);

        acct.wlock.unlock();
        branch.rlock.unlock();
        b.rlock.unlock();
    }
    
    public void sumOneBranch(Bank b, Random r) {
        int branchId = r.nextInt(NUM_BRANCHES);
        
        b.rlock.lock();
        Branch branch = b.branches[branchId];
        branch.rlock.lock();
        
        for (int i=0; i<NUM_ACCTS_PER_BRANCH; i++) {
            branch.accounts[i].rlock.lock();
        }
        
        branch.sumBalances();

        for (int i=0; i<NUM_ACCTS_PER_BRANCH; i++) {
            branch.accounts[i].rlock.unlock();
        }
        
        branch.rlock.unlock();
        b.rlock.unlock();
    }
    
    public void sumAll(Bank b) {
        b.rlock.lock();
        
        // lock all branches (and all accounts in branches)
        for (int i=0; i<NUM_BRANCHES; i++) {
            Branch branch = b.branches[i];
            branch.rlock.lock();
            for (int j=0; j<NUM_ACCTS_PER_BRANCH; j++) {
                branch.accounts[j].rlock.lock();
            }
        }
        
        b.sumAll();
        
        // unlock all branches (and all accounts in branches)
        for (int i=0; i<NUM_BRANCHES; i++) {        
            Branch branch = b.branches[i];
            for (int j=0; j<NUM_ACCTS_PER_BRANCH; j++) {
                branch.accounts[j].rlock.unlock();
            }
            branch.rlock.unlock();
        }

        b.rlock.unlock();
    }
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        int numWithdraw = Integer.parseInt(args[0]);
        int numDeposit = Integer.parseInt(args[1]);
        int numBranch = Integer.parseInt(args[2]);
        int numBank = Integer.parseInt(args[3]);
        BankTest b = new BankTestRWLock(numWithdraw, numDeposit, numBranch, numBank);
        b.runExperiment();
    }
    
}
