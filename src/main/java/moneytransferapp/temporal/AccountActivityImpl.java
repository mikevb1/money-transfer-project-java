// @@@SNIPSTART money-transfer-java-activity-implementation
package moneytransferapp.temporal;

import io.temporal.activity.*;

public class AccountActivityImpl implements AccountActivity {
    // Mock up the withdrawal of an amount of money from the source account
    @Override
    public void withdraw(String accountId, String referenceId, double amount) {
        System.out.printf("\nWithdrawing €%.2f from account %s.\n[ReferenceId: %s]\n",
                amount, accountId, referenceId);
        System.out.flush();
    }

    // Mock up the deposit of an amount of money from the destination account
    @Override
    public void deposit(String accountId, String referenceId, double amount) {
        boolean activityShouldSucceed = true;

        if (!activityShouldSucceed) {
            System.out.println("Deposit failed");
            System.out.flush();
            throw Activity.wrap(new RuntimeException("Simulated Activity error during deposit of funds"));
        }

        System.out.printf("\nDepositing €%.2f into account %s.\n[ReferenceId: %s]\n",
                amount, accountId, referenceId);
        System.out.flush();
    }

    // Mock up a compensation refund to the source account
    @Override
    public void refund(String accountId, String referenceId, double amount) {
        boolean activityShouldSucceed = true;

        if (!activityShouldSucceed) {
            System.out.println("Refund failed");
            System.out.flush();
            throw Activity.wrap(new RuntimeException("Simulated Activity error during refund to source account"));
        }

        System.out.printf("\nRefunding €%.2f to account %s.\n[ReferenceId: %s]\n",
                amount, accountId, referenceId);
        System.out.flush();
    }
}
// @@@SNIPEND
