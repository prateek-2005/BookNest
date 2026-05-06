package com.booknest.wallet;

import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.repository.WalletRepository;
import com.booknest.wallet.repository.StatementRepository;
import com.booknest.wallet.service.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeposit_Success() {
        Wallet wallet = new Wallet();
        wallet.setWalletId(1);
        wallet.setUserId(1L);
        wallet.setCurrentBalance(100.0);

        // Impl uses findByWalletIdForUpdate
        when(walletRepository.findByWalletIdForUpdate(1)).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.addMoney(1, 50.0);
        assertEquals(150.0, result.getCurrentBalance());
    }

    @Test
    void testPayMoney_InsufficientBalance() {
        Wallet wallet = new Wallet();
        wallet.setWalletId(1);
        wallet.setUserId(1L);
        wallet.setCurrentBalance(20.0);

        when(walletRepository.findByWalletIdForUpdate(1)).thenReturn(wallet);

        assertThrows(RuntimeException.class, () -> walletService.payMoney(1, 50.0, 123));
    }
}
