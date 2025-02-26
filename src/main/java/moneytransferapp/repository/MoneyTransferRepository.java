package moneytransferapp.repository;

import moneytransferapp.model.MoneyTransferWorkFlowModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MoneyTransferRepository extends JpaRepository<MoneyTransferWorkFlowModel, UUID> {

    Optional<MoneyTransferWorkFlowModel> findByTransactionReference(String transactionReference);

}
