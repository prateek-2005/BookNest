package com.booknest.wallet.service;

import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.repository.WalletRepository;
import com.booknest.wallet.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

	private final WalletRepository walletRepository;
	private final StatementRepository statementRepository;

	public WalletServiceImpl(WalletRepository walletRepository, StatementRepository statementRepository) {
		this.walletRepository = walletRepository;
		this.statementRepository = statementRepository;
	}

	@Override
	public List<Wallet> getWallets() {
		return walletRepository.findAll();
	}

	@Override
	public Wallet addWallet(Wallet wallet) {
		wallet.setCurrentBalance(0.0);
		return walletRepository.save(wallet);
	}

	@Override
	@Transactional
	public Wallet addMoney(Integer walletId, Double amount) {
		Wallet wallet = walletRepository.findByWalletIdForUpdate(walletId);
		if (wallet == null)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Wallet not found");
		if (amount == null || amount <= 0)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Amount must be greater than 0");

		wallet.setCurrentBalance(wallet.getCurrentBalance() + amount);
		walletRepository.save(wallet);

		Statement stmt = new Statement();
		stmt.setTransactionType("DEPOSIT");
		stmt.setAmount(amount);
		stmt.setTransactionRemarks("Deposit of " + amount + " INR");
		stmt.setWallet(wallet);
		// dateTime will be auto-set in Statement entity @PrePersist
		statementRepository.save(stmt);

		return wallet;
	}

	@Override
	@Transactional
	public Wallet payMoney(Integer walletId, Double amount, Integer orderId) {
		Wallet wallet = walletRepository.findByWalletIdForUpdate(walletId);
		if (wallet == null)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Wallet not found");
		if (amount == null || amount <= 0)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
		if (wallet.getCurrentBalance() < amount)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Insufficient balance");

		wallet.setCurrentBalance(wallet.getCurrentBalance() - amount);
		walletRepository.save(wallet);

		Statement stmt = new Statement();
		stmt.setTransactionType("WITHDRAW");
		stmt.setAmount(amount);
		stmt.setOrderId(orderId);
		stmt.setTransactionRemarks(
				orderId == null
						? ("Withdraw of " + amount + " INR")
						: ("Payment of " + amount + " INR for order #" + orderId)
		);
		stmt.setWallet(wallet);
		statementRepository.save(stmt);

		return wallet;
	}

	@Override
	public Wallet getById(Integer walletId) {
		return walletRepository.findByWalletId(walletId);
	}

	@Override
	public Wallet getByUserId(Long userId) {
		return walletRepository.findByUserId(userId);
	}

	@Override
	@Transactional
	public Wallet getOrCreateByUserId(Long userId) {
		Wallet existing = walletRepository.findByUserIdForUpdate(userId);
		if (existing != null) return existing;
		Wallet wallet = new Wallet();
		wallet.setUserId(userId);
		wallet.setCurrentBalance(0.0);
		return walletRepository.save(wallet);
	}

	@Override
	@Transactional
	public Wallet addMoneyByUserId(Long userId, Double amount) {
		Wallet wallet = getOrCreateByUserId(userId);
		return addMoney(wallet.getWalletId(), amount);
	}

	@Override
	@Transactional
	public Wallet payMoneyByUserId(Long userId, Double amount, Integer orderId) {
		Wallet wallet = getOrCreateByUserId(userId);
		return payMoney(wallet.getWalletId(), amount, orderId);
	}

	@Override
	public List<Statement> getStatementsById(Integer walletId) {
		return statementRepository.findByWallet_WalletId(walletId);
	}

	@Override
	public List<Statement> getStatementsByUserId(Long userId) {
		Wallet wallet = walletRepository.findByUserId(userId);
		if (wallet == null) throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Wallet not found");
		return statementRepository.findByWallet_WalletId(wallet.getWalletId());
	}

	@Override
	public List<Statement> getAllStatements() {
		return statementRepository.findAll();
	}

	@Override
	public void deleteById(Integer walletId) {
		walletRepository.deleteById(walletId);
	}
}
