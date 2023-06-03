package com.capmation.challenge1;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    void shouldReturnABankAccountWhenDataIsSaved() {
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
    void shouldNotReturnABankAccountWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts/1009", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }
    
    @Test
    void shouldReturnAllBankAccountsWhenListIsRequested() {
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
    @DirtiesContext
    void shouldCreateANewBankAccount() {
    	BankAccount newBankAccount = new BankAccount(null, 0.00, "SAVINGS", "user1");
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .postForEntity("/bankaccounts", newBankAccount, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");
        String accountType = documentContext.read("$.accountType");
        String owner = documentContext.read("$.owner");
        
        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(0.00);
        assertThat(accountType).isEqualTo("SAVINGS");
        assertThat(owner).isEqualTo("user1");
    }
    
    @Test
    @DirtiesContext
    void shouldDepositMoneyInBankAccount() {
    	//TODO: Do a normal deposit into any bank account and validate expected new account amount value
    	DepositRecord depositRecord = new DepositRecord(1001L, 345.50, new Date());
    	HttpEntity<DepositRecord> request = new HttpEntity<>(depositRecord);
    	ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .exchange("/bankaccounts/1001/deposit", HttpMethod.PUT, request ,Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts/1001", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // New account balance should be: 1345.50
        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(1345.50);
    }
    
    @Test
    @DirtiesContext
    void shouldWithdrawMoneyFromBankAccount() {
    	//TODO: Do a normal withdrawal from one bank account and validate expected new account amount value
    	WithdrawalRecord withdrawalRecord = new WithdrawalRecord(1001L, 250.00, new Date());
    	HttpEntity<WithdrawalRecord> request = new HttpEntity<>(withdrawalRecord);
    	ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .exchange("/bankaccounts/1001/withdrawal", HttpMethod.PUT, request ,Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts/1001", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // New account balance should be: 750.00
        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(750.00);
    }
    
    @Test
    void shouldTransferMoneyInBankAccount() {
    	//TODO: Do a normal deposit into any bank account and validate expected new account amount value
    	TransferenceRecord transferenceRecord = new TransferenceRecord(1001L, 500.00, new Date());
    	HttpEntity<TransferenceRecord> request = new HttpEntity<>(transferenceRecord);
    	ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("user2", "user2$$pwd")
                .exchange("/bankaccounts/1004/transference", HttpMethod.PUT, request ,Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("user1", "user1$$pwd")
                .getForEntity("/bankaccounts/1001", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // New account balance should be: 1500.00
        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(1500.00);
        
        response = restTemplate
                .withBasicAuth("user2", "user2$$pwd")
                .getForEntity("/bankaccounts/1004", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        documentContext = JsonPath.parse(response.getBody());
        // New account balance should be: 4200.50
        amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(4200.50);
    }

}
