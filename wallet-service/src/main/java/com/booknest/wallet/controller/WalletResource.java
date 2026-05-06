package com.booknest.wallet.controller;

import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallet")
public class WalletResource {

    private final WalletService walletService;

    public WalletResource(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public List<Wallet> getWallets() {
        return walletService.getWallets();
    }

    @GetMapping("/statements")
    public List<Statement> getAllStatements() {
        return walletService.getAllStatements();
    }

    @GetMapping("/id/{walletId}")
    public Wallet getWalletById(@PathVariable String walletId) {
        try {
            return walletService.getById(Integer.parseInt(walletId));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid wallet ID format");
        }
    }

    @GetMapping("/user/{userId}")
    public Wallet getWalletByUser(@PathVariable Long userId) {
        Wallet wallet = walletService.getByUserId(userId);
        if (wallet == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found");
        return wallet;
    }

    @PostMapping("/user/{userId}")
    public Wallet getOrCreateWallet(@PathVariable Long userId) {
        return walletService.getOrCreateByUserId(userId);
    }

    @PutMapping("/user/{userId}/addMoney")
    public Wallet addMoneyByUser(@PathVariable Long userId, @RequestParam Double amount) {
        return walletService.addMoneyByUserId(userId, amount);
    }

    @PutMapping("/user/{userId}/payMoney")
    public Wallet payMoneyByUser(
            @PathVariable Long userId,
            @RequestParam Double amount,
            @RequestParam(required = false) Integer orderId
    ) {
        return walletService.payMoneyByUserId(userId, amount, orderId);
    }

    @GetMapping("/user/{userId}/statements")
    public List<Statement> getStatementsByUser(@PathVariable Long userId) {
        return walletService.getStatementsByUserId(userId);
    }


    @PostMapping("/addNewWallet")
    public Wallet addWallet(@RequestBody Wallet wallet) {
        return walletService.addWallet(wallet);
    }

    @PutMapping("/id/{walletId}/addMoney")
    public Wallet addMoney(@PathVariable String walletId, @RequestParam Double amount) {
        try {
            return walletService.addMoney(Integer.parseInt(walletId), amount);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid wallet ID format");
        }
    }

    @PutMapping("/id/{walletId}/payMoney")
    public Wallet payMoney(@PathVariable String walletId, @RequestParam Double amount) {
        try {
            return walletService.payMoney(Integer.parseInt(walletId), amount, null);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid wallet ID format");
        }
    }

    @GetMapping("/id/{walletId}/statements")
    public List<Statement> getStatements(@PathVariable String walletId) {
        try {
            return walletService.getStatementsById(Integer.parseInt(walletId));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid wallet ID format");
        }
    }

    @DeleteMapping("/id/{walletId}")
    public void deleteWallet(@PathVariable String walletId) {
        try {
            walletService.deleteById(Integer.parseInt(walletId));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid wallet ID format");
        }
    }
}
