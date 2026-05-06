package com.booknest.wallet.service;

import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.entity.Statement;
import java.util.List;

public interface WalletService {
    List<Wallet> getWallets();
    Wallet addWallet(Wallet wallet);
    Wallet addMoney(Integer walletId, Double amount);
    Wallet payMoney(Integer walletId, Double amount, Integer orderId);
    Wallet getById(Integer walletId);
    Wallet getByUserId(Long userId);
    Wallet getOrCreateByUserId(Long userId);
    Wallet addMoneyByUserId(Long userId, Double amount);
    Wallet payMoneyByUserId(Long userId, Double amount, Integer orderId);
    List<Statement> getStatementsById(Integer walletId);
    List<Statement> getStatementsByUserId(Long userId);
    List<Statement> getAllStatements();
    void deleteById(Integer walletId);
}
