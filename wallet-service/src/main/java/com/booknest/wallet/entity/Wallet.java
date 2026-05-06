package com.booknest.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walletId;

    @Column(nullable = false, unique = true)
    private Long userId;

    private Double currentBalance;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Statement> statements;

    // --- Getters & Setters ---
    public Integer getWalletId() {
        return walletId;
    }

    public void setWalletId(Integer walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }
}
