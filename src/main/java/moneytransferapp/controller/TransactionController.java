package moneytransferapp.controller;
import moneytransferapp.model.MoneyTransferWorkFlowModel;
import moneytransferapp.temporal.TransactionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import moneytransferapp.service.MoneyTransferService;

import java.util.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final MoneyTransferService moneyTransferService;

    @Autowired
    public TransactionController(MoneyTransferService moneyTransferService) {
        this.moneyTransferService = moneyTransferService;
    }

    @PostMapping("/start")
    public Map<String, String> startTransaction() {

        return moneyTransferService.startTransaction();

    }

    @GetMapping
    public List<MoneyTransferWorkFlowModel> getAllTransactions() {
        // Fetch saved transactions from the repository
        return moneyTransferService.getTransactions();
    }

    @PutMapping("/approve")
    public String approveTransaction(@RequestParam String transactionReference) {

        return moneyTransferService.approveTransaction(transactionReference);
    }

    @PutMapping("/disapprove")
    public String disapproveTransaction(@RequestParam String transactionReference) {

        return moneyTransferService.disapproveTransaction(transactionReference);
    }
}
