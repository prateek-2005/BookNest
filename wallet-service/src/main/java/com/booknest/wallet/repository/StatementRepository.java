package com.booknest.wallet.repository;

import com.booknest.wallet.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatementRepository extends JpaRepository<Statement, Integer> {
	List<Statement> findByWallet_WalletId(Integer walletId);
    List<Statement> findByTransactionType(String transactionType);
    List<Statement> findByOrderId(Integer orderId);
}
