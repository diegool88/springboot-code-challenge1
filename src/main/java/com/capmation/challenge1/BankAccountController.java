package com.capmation.challenge1;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
    private ResponseEntity<Void> createBankAccount(@RequestBody BankAccount newBankAccountRequest, UriComponentsBuilder ucb, Principal principal) {
        BankAccount savedBankAccount = bankAccountRepository.save(new BankAccount(null, 0.00, newBankAccountRequest.accountType(), principal.getName()));
        URI locationOfNewBankAccount = ucb
                .path("bankaccounts/{id}")
                .buildAndExpand(savedBankAccount.id())
                .toUri();
        return ResponseEntity.created(locationOfNewBankAccount).build();
    }
	
	@PatchMapping("/{requestedId}/deposit")
    private ResponseEntity<Void> patchDepositInBankAccount(@PathVariable Long requestedId, @RequestBody DepositRecord depositRecord) {
		/* TODO
		 * You need to: 
		 * 1. Verify the existence of bank account. (Any role can make a deposit)
		 * 2. Update the bank account amount with the given deposit amount.
		 * 3. Return OK response code (200) to the consumer with the updated resource in the response body.
		 * 4. If the account was not found return the corresponding HTTP response.
		 * */
		if(depositInBankAccount(requestedId, depositRecord.amount()) != null) {
			return ResponseEntity.noContent().build();		
		}
		return ResponseEntity.notFound().build();
    }
	
	@PatchMapping("/{requestedId}/withdrawal")
    private ResponseEntity<Void> patchWithdrawalInBankAccount(@PathVariable Long requestedId, @RequestBody WithdrawalRecord withdrawalRecord, Principal principal) {
		/* TODO
		 * You need to: 
		 * 1. Verify the existence of bank account. (Only the bank account owner is able to perform a withdrawal)
		 * 2. Update the bank account amount with the given withdrawal amount.
		 * 3. Return OK response code (200) to the consumer with the updated resource in the response body.
		 * 4. If the account was not found return the corresponding HTTP response. If the user trying to make the withdrawal is not the owner
		 * then return a not found response as well.
		 * */
		if (withdrawInBankAccount(requestedId, withdrawalRecord.amount(), principal) != null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
    }
	
	@PatchMapping("/{requestedId}/transference")
    private ResponseEntity<Void> patchTransferenceInBankAccount(@PathVariable Long requestedId, @RequestBody TransferenceRecord transferenceRecord, Principal principal) {
		/* TODO
		 * You need to: 
		 * 1. Verify the existence of bank account. (Only the bank account owner is able to perform a transference)
		 * 2. Update the bank account amount with the given transference amount.
		 * 3. Update the destination account to be updated like a normal deposit.
		 * 3. Return OK response code (200) to the consumer with the updated resource in the response body.
		 * 4. If the account was not found return the corresponding HTTP response. If the user trying to make the withdrawal is not the owner
		 * then return a not found response as well.
		 * */
		//Validate destination bank account & Debit source account
		if(bankAccountRepository.findById(transferenceRecord.destinationId()).isPresent() && withdrawInBankAccount(requestedId, transferenceRecord.amount(), principal) != null) {
			//credit destination account
			depositInBankAccount(transferenceRecord.destinationId(), transferenceRecord.amount());
			return ResponseEntity.noContent().build();		
		}
		return ResponseEntity.notFound().build();
    }
	
	private BankAccount withdrawInBankAccount(Long requestedId, Double amount, Principal principal) {
		BankAccount bankAccount = findBankAccount(requestedId, principal);
		if(bankAccount != null) {
			BankAccount updatedBankAccount = new BankAccount(bankAccount.id(), bankAccount.amount() - amount, bankAccount.accountType(), bankAccount.owner());
			bankAccountRepository.save(updatedBankAccount);
			return updatedBankAccount;		
		}
		return null;
	}
	
	private BankAccount depositInBankAccount(Long requestedId, Double amount) {
		Optional<BankAccount> bankAccount = bankAccountRepository.findById(requestedId);
		if(bankAccount.isPresent()) {
			BankAccount updatedBankAccount = new BankAccount(bankAccount.get().id(), bankAccount.get().amount() + amount, bankAccount.get().accountType(), bankAccount.get().owner());
			bankAccountRepository.save(updatedBankAccount);
			return 	updatedBankAccount;
		}
		return null;
	}
	
	private BankAccount findBankAccount(Long requestedId, Principal principal) {
        return bankAccountRepository.findByIdAndOwner(requestedId, principal.getName());
    }

}
