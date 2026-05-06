package com.booknest.wallet;

import com.booknest.wallet.controller.WalletResource;
import com.booknest.wallet.service.WalletService;
import com.booknest.wallet.entity.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(WalletResource.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    public void testGetWalletByUser() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setUserId(1L);
        wallet.setCurrentBalance(1000.0);

        // Fix: Method name is getByUserId
        when(walletService.getByUserId(1L)).thenReturn(wallet);

        // Fix: Path is /wallet/user/{userId}
        mockMvc.perform(get("/wallet/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(1000.0));
    }
}
