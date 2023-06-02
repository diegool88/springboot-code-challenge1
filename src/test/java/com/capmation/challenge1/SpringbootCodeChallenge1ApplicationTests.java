package com.capmation.challenge1;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import net.minidev.json.JSONArray;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringbootCodeChallenge1ApplicationTests {
	
	@Autowired
    TestRestTemplate restTemplate;

	@Test
    @DirtiesContext
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts/1001", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1001);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(1000.00);
        
        String accountType = documentContext.read("$.accountType");
        assertThat(accountType).isEqualTo("SAVINGS");
        
        String owner = documentContext.read("$.owner");
        assertThat(owner).isEqualTo("user1");
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts/1009", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }
    
    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(1001, 1002, 1003);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(1000.00, 10.00, 1500.00);
    }
    
    @Test
    void shouldCreateANewBankAccount() {
    	//TODO: Create a new bank account using user1 and validate that the new location created is available
    }
    
    @Test
    void shouldDepositMoneyInBankAccount() {
    	//TODO: Do a normal deposit into any bank account and validate expected new account amount value
    }
    
    @Test
    void shouldWithdrawMoneyFromBankAccount() {
    	//TODO: Do a normal withdrawal from one bank account and validate expected new account amount value
    }
    
    @Test
    void shouldTransferMoneyInBankAccount() {
    	//TODO: Do a normal deposit into any bank account and validate expected new account amount value
    }

}
