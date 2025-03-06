package moneytransferapp.controller;

import moneytransferapp.dto.TransactionRequest;
import moneytransferapp.model.MoneyTransferWorkFlowModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import moneytransferapp.service.MoneyTransferService;

import java.util.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final MoneyTransferService moneyTransferService;

    @Autowired
    public TransactionController(MoneyTransferService moneyTransferService) {
        this.moneyTransferService = moneyTransferService;
    }

    @PostMapping("/start")
    public String startTransaction() {

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

    @PostMapping("/requestTransaction")
    public String requestTransaction(@RequestBody TransactionRequest request) {

        return moneyTransferService.requestTransfer(request);
    }
}
