// @@@SNIPSTART money-transfer-java-workflow-interface
package moneytransferapp.temporal;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MoneyTransferWorkflow {
    // The Workflow Execution that starts this method can be initiated from code or
    // from the 'temporal' CLI utility.
    @WorkflowMethod
    void transfer(TransactionDetails transaction);

    // New methods for approval/disapproval
    @SignalMethod
    void approveTransaction(TransactionDetails transaction);

    @SignalMethod
    void disapproveTransaction(TransactionDetails transaction);
}
// @@@SNIPEND
