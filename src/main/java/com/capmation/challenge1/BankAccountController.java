package com.capmation.challenge1;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/bankaccounts")
public class BankAccountController {
	
	private BankAccountRepository bankAccountRepository;

	public BankAccountController(BankAccountRepository bankAccountRepository) {
		this.bankAccountRepository = bankAccountRepository;
	}
	
	@GetMapping("/{requestedId}")
    public ResponseEntity<BankAccount> findById(@PathVariable Long requestedId, Principal principal) {
		BankAccount bankAccount = findBankAccount(requestedId, principal);
        if (bankAccount != null) {
            return ResponseEntity.ok(bankAccount);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
	
	@GetMapping
    public ResponseEntity<List<BankAccount>> findAll(Pageable pageable, Principal principal) {
        Page<BankAccount> page = bankAccountRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }
	
	@PostMapping
    private ResponseEntity<Void> createBankAccount(@RequestBody BankAccount newBankAccountRequest, UriComponentsBuilder ucb) {
        BankAccount savedBankAccount = null; //TODO: Using the repository create a new bank account
        URI locationOfNewBankAccount = ucb
                .path("bankaccounts/{id}")
                .buildAndExpand(savedBankAccount.id())
                .toUri();
        return ResponseEntity.created(locationOfNewBankAccount).build();
    }
	
	@PatchMapping("/{requestedId}/deposit")
    private ResponseEntity<Void> putDepositInBankAccount(@PathVariable Long requestedId, @RequestBody DepositRecord depositRecord) {
		/* TODO
		 * You need to: 
		 * 1. Verify the existence of bank account. (Any role can make a deposit)
		 * 2. Update the bank account amount with the given deposit amount.
		 * 3. Return OK response code (200) to the consumer with the updated resource in the response body.
		 * 4. If the account was not found return the corresponding HTTP response.
		 * */
		return ResponseEntity.badRequest().build();
    }
	
	@PatchMapping("/{requestedId}/withdrawal")
    private ResponseEntity<Void> putWithdrawalInBankAccount(@PathVariable Long requestedId, @RequestBody WithdrawalRecord withdrawalRecord) {
		/* TODO
		 * You need to: 
		 * 1. Verify the existence of bank account. (Only the bank account owner is able to perform a withdrawal)
		 * 2. Update the bank account amount with the given withdrawal amount.
		 * 3. Return OK response code (200) to the consumer with the updated resource in the response body.
		 * 4. If the account was not found return the corresponding HTTP response. If the user trying to make the withdrawal is not the owner
		 * then return a not found response as well.
		 * */
		return ResponseEntity.badRequest().build();
    }
	
	@PatchMapping("/{requestedId}/tranference")
    private ResponseEntity<Void> putTransferenceInBankAccount(@PathVariable Long requestedId, @RequestBody TransferenceRecord transferenceRecord) {
		/* TODO
		 * You need to: 
		 * 1. Verify the existence of bank account. (Only the bank account owner is able to perform a transference)
		 * 2. Update the bank account amount with the given transference amount.
		 * 3. Update the destination account to be updated like a normal deposit.
		 * 3. Return OK response code (200) to the consumer with the updated resource in the response body.
		 * 4. If the account was not found return the corresponding HTTP response. If the user trying to make the withdrawal is not the owner
		 * then return a not found response as well.
		 * */
		return ResponseEntity.badRequest().build();
    }
	
	private BankAccount findBankAccount(Long requestedId, Principal principal) {
        return bankAccountRepository.findByIdAndOwner(requestedId, principal.getName());
    }

}
