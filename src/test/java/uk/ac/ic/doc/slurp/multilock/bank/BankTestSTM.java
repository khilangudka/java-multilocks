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

import org.deuce.Atomic;

public class BankTestSTM extends BankTest {

    public BankTestSTM(int nWithdraw, int nDeposit, int nBranch, int nBank) {
        super(nWithdraw, nDeposit, nBranch, nBank);
    }
    
    @Atomic
    public void withdraw(Bank b, Random r) {
        int branchId = r.nextInt(NUM_BRANCHES);
        int acctId = r.nextInt(NUM_ACCTS_PER_BRANCH);
        int amt = r.nextInt(60);

        Branch branch = b.branches[branchId];
        Account acct = branch.accounts[acctId];
        acct.withdraw(amt);
    }
    
    @Atomic
    public void deposit(Bank b, Random r) {
        int branchId = r.nextInt(NUM_BRANCHES);
        int acctId = r.nextInt(NUM_ACCTS_PER_BRANCH);
        int amt = r.nextInt(60);

        Branch branch = b.branches[branchId];
        Account acct = branch.accounts[acctId];
        acct.deposit(amt);
    }

    @Atomic
    public void sumOneBranch(Bank b, Random r) {
        int branchId = r.nextInt(NUM_BRANCHES);
        Branch branch = b.branches[branchId];       
        branch.sumBalances();
    }

    @Atomic    
    public void sumAll(Bank b) {
        b.sumAll();
    }
    
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        int numWithdraw = Integer.parseInt(args[0]);
        int numDeposit = Integer.parseInt(args[1]);
        int numBranch = Integer.parseInt(args[2]);
        int numBank = Integer.parseInt(args[3]);
        BankTest b = new BankTestSTM(numWithdraw, numDeposit, numBranch, numBank);
        b.runExperiment();
    }

}
