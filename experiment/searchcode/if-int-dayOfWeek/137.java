package me.itzg.kidsbank.services;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import me.itzg.kidsbank.CurrencyUtil;
import me.itzg.kidsbank.KidsbankException;
import me.itzg.kidsbank.domain.Account;
import me.itzg.kidsbank.domain.Action;
import me.itzg.kidsbank.domain.AllowanceSpec;
import me.itzg.kidsbank.domain.Parent;
import me.itzg.kidsbank.domain.Transaction;
import me.itzg.kidsbank.repositories.AccountsRepository;
import me.itzg.kidsbank.repositories.ParentsRepository;
import me.itzg.kidsbank.repositories.TransactionsRepository;
import me.itzg.kidsbank.shared.AccountSummary;
import me.itzg.kidsbank.shared.LocaleSpec;
import me.itzg.kidsbank.shared.TransactionSummary;
import me.itzg.kidsbank.shared.TransactionSummaryPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {
	private static final Logger logger = LoggerFactory
			.getLogger(AccountsService.class);
	
	@Autowired
	private ParentsRepository parentsRepository;
	
	@Autowired
	private AccountsRepository accountsRepository;
	
	@Autowired
	private TransactionsRepository transactionsRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing Transaction collection");
		// With a freshly created DB there wasn't a chance for Spring Data to
		// see that Transaction documents were needed. Retrieval of account balances would fail
		// with that document type absent since it is doing a raw map-reduce.
		if (!mongoTemplate.collectionExists(Transaction.class)) {
			mongoTemplate.createCollection(Transaction.class);
		}
	}
	
	public Account createAccount(Locale browserLocale, Parent parent, String name) {
		Account account = new Account();
		account.setName(name);
		
		parent = enlivenParent(parent);
		
		Query dupNameQuery = Query.query(Criteria.where("name").is(name));
		dupNameQuery.fields().include("name");
		List<Account> sameNameAccounts = mongoTemplate.find(dupNameQuery, Account.class);
		if (!sameNameAccounts.isEmpty()) {
			List<String/*account id*/> accountIds = new ArrayList<String>(sameNameAccounts.size());
			for (Account accountResponse : sameNameAccounts) {
				accountIds.add(accountResponse.getId());
			}
			long existingOverlaps = mongoTemplate
					.count(Query
							.query(Criteria.where("accounts").in(accountIds)), 
							Parent.class);
			if (existingOverlaps > 0) {
				return null;
			}
		}
		
		if (parent.getDefaultLocale() != null) {
			account.setLocale(parent.getDefaultLocale());
		}
		else {
			account.setLocale(new LocaleSpec(browserLocale));
		}

		account = accountsRepository.save(account);
		mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(parent.getOpenId())), 
				new Update().push("accounts", account.getId()), Parent.class);
		
		return account;
	}

	public List<AccountSummary> getAccountSummaries(Parent parentIn) throws KidsbankException {
		// first find the parent's list of linked accounts
		Parent parent = parentsRepository.findJustAccounts(parentIn.getOpenId());
		
		if (parent.getAccounts() == null || parent.getAccounts().isEmpty()) {
			return Collections.emptyList();
		}
		
		Query accountsQuery = Query.query(Criteria.where("_id").in(parent.getAccounts()));
		accountsQuery.sort().on("name", Order.ASCENDING);
		
		List<AccountSummary> summaries = new ArrayList<AccountSummary>();
		for (Account account : mongoTemplate.find(accountsQuery, Account.class)) {
			AccountSummary summary = new AccountSummary();
			summary.setName(account.getName());
			summary.setId(account.getId());
			// TODO need to summarize real amount
			summary.setBalance(CurrencyUtil.format(account.getLocale(), getAccountBalance(account.getId())));
			summaries.add(summary);
		}
		
		return summaries;
	}
	
	public double getAccountBalance(String accountId) {
		MapReduceResults<ValueCount> results = mongoTemplate.mapReduce(Query.query(Criteria.where("accountId").is(accountId)), 
				"transaction", 
				"function() { emit('amount',this.amount); }", 
				"function(key,values){var sum = 0; for (var i = 0; i < values.length; ++i) { sum += values[i] } return sum;}", ValueCount.class);
		
		if (results.getCounts().getOutputCount() > 0) {
			return results.iterator().next().getValue();
		}
		else {
			return 0;
		}
	}
	
	public String addTransaction(String accountId, Parent parent, Date date,
			String description, Double income, Double expense) throws KidsbankException {
		Account account = accountsRepository.findById(accountId);
		return addTransaction(account, parent, date, description, income, expense);
	}
	
	public String addTransaction(Account account, Parent parent, Date date,
			String description, Double income, Double expense) throws KidsbankException {
		if (income == null && expense == null) {
			throw new KidsbankException("Both income and expense values were null");
		}

		Transaction tx = new Transaction();
		tx.setAccount(account);
		tx.setParent(parent);
		fillTransaction(tx, date, description, income, expense);
		
		Transaction resultTx = transactionsRepository.save(tx);
		
		return resultTx.getId();
	}

	public void deleteTransaction(String accountId, String transactionId,
			Parent parent) throws KidsbankException {
		
		Transaction transaction = transactionsRepository.findOne(transactionId);
		if (accountId.equals(transaction.getAccountId())) {
			transactionsRepository.delete(transaction);
		
			recordAction(accountId, parent, "deleteTransaction", transaction.toString(), null);
		}
		else {
			logger.warn("Trying to delete transaction {} from wrong account {}", transaction, accountId);
			throw new KidsbankException("Wrong account for transaction");
		}
	}

	public void editTransaction(String accountId, String transactionId,
			Parent parent, Date date, String description, Double income,
			Double expense) throws KidsbankException {
		if (income == null && expense == null) {
			throw new KidsbankException("Both income and expense values were null");
		}
		
		Transaction tx = transactionsRepository.findOne(transactionId);
		if (tx == null) {
			throw new KidsbankException("Unable to find transaction");
		}
		
		if (!tx.getAccountId().equals(accountId)) {
			throw new KidsbankException("Wrong account for transaction");
		}
		
		fillTransaction(tx, date, description, income, expense);
		
		transactionsRepository.save(tx);
		
		recordAction(accountId, parent, "editTransaction", null, tx.toString());
	}

	private void fillTransaction(Transaction tx, Date date, String description,
			Double income, Double expense) {
		tx.setWhen(date);
		tx.setDescription(description);
		tx.setAmount(income != null ? income : -1*expense);
	}

	public TransactionSummaryPage getTransactionSummaries(String accountId, Pageable pageableIn) {
		Pageable pg = new PageRequest(pageableIn.getPageNumber(), pageableIn.getPageSize(), 
				Sort.Direction.DESC, "when", "_id");
		
		
		Page<Transaction> result = transactionsRepository.findByAccountId(accountId, pg);
		
		TransactionSummaryPage txPage = new TransactionSummaryPage();
		txPage.setCurrentPage(result.getNumber());
		txPage.setTotalPages(result.getTotalPages());
		
		List<TransactionSummary> summaries = new ArrayList<TransactionSummary>();
		txPage.setContent(summaries);
		
		if (result.getContent() != null && !result.getContent().isEmpty()) {
			Account account = accountsRepository.findById(accountId);
		
			Locale locale = account.getLocale().toLocale();
			
			final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
			
			List<Transaction> content = result.getContent();
			for (Transaction transaction : content) {
				TransactionSummary txSummary = new TransactionSummary();
				
				txSummary.setId(transaction.getId());
				if (transaction.getWhen() != null) {
					txSummary.setWhen(dateFormat.format(transaction.getWhen()));
				}
				txSummary.setDescription(transaction.getDescription());
				txSummary.setAmountValue(transaction.getAmount());
				txSummary.setAmount(currencyFormat.format(transaction.getAmount()));

				summaries.add(txSummary);
			}
		}
		
		return txPage;
	}

	public List<String> getDescriptionSuggestions(String accountId, String term) {
		MapReduceResults<ValueCount> results = mongoTemplate.mapReduce(Query.query(Criteria.where("accountId").is(accountId).and("description").regex("^"+term, "i")), 
				"transaction", 
				"function() { emit(this.description,1); }", 
				"function(key,values){var sum = 0; for (var i = 0; i < values.length; ++i) { sum += values[i] } return sum;}", ValueCount.class);
		
		List<String> suggestions = new ArrayList<String>();
		for (ValueCount valueCount : results) {
			suggestions.add(valueCount.getId());
		}
		Collections.sort(suggestions);
		
		return suggestions;
	}
	
	@SuppressWarnings("unused")
	private static final class ValueCount {
		private String id;
		
		private double value;

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	public void detachAccount(String accountId, Parent parent) {
		mongoTemplate.updateFirst(Query.query(Criteria.where("openId").is(parent.getOpenId())), 
				new Update().pull("accounts", accountId), 
				Parent.class);
		
		recordAction(accountId, parent, "detachAccount");
	}

	private void recordAction(String accountId, Parent parent, String operation, Object oldValue, Object newValue) {
		Action action = new Action();
		
		action.setTimestamp(new Date());
		action.setOperation(operation);
		action.setParentId(parent.getOpenId());
		action.setTarget(accountId);
		action.setNewValue(newValue != null ? newValue.toString() : null);
		action.setOldValue(oldValue != null ? oldValue.toString(): null);
		
		mongoTemplate.insert(action);
	}
	
	private void recordAction(String accountId, Parent parent, String operation) {
		recordAction(accountId, parent, operation, null, null);
	}

	public void rename(String accountId, Parent parent, String name) {
		mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(accountId)), new Update().set("name", name), Account.class);
		
		recordAction(accountId, parent, "renameAccount", null, name);
	}

	public void deactiveAllowance(Parent parent, String accountId) throws KidsbankException {
		Account account = accountsRepository.findById(accountId);
		
		if (account != null) {
			// Only deactivate if it wasn't already
			if (account.getAllowance() != null) {
				account.setAllowance(null);
				accountsRepository.save(account);
				recordAction(accountId, parent, "deactiveAllowance");
			}
		}
		else {
			throw new KidsbankException("Could not find account for "+accountId);
		}
		
	}

	public void configureAllowance(Parent parent, String accountId, int dayOfWeek,
			Double amount, String description) throws KidsbankException {
		if (dayOfWeek < 0 || dayOfWeek > 7) {
			throw new KidsbankException("day was out of range: "+dayOfWeek);
		}
		if (amount == null) {
			throw new KidsbankException("amount was null");
		}
		
		Account account = accountsRepository.findById(accountId);
		
		if (account != null) {
			AllowanceSpec allowance = new AllowanceSpec();
			allowance.setDayOfWeek(dayOfWeek);
			allowance.setAmount(amount);
			allowance.setParentId(parent.getOpenId());
			allowance.setDescription(description);
			account.setAllowance(allowance);
			accountsRepository.save(account);
			
			recordAction(accountId, parent, "configureAllowance", null, Integer.toString(dayOfWeek)+" "+amount);
		}
		else {
			throw new KidsbankException("Could not find account for "+accountId);
		}
	}

	public AllowanceSpec getAllowance(String accountId) {
		Account account = accountsRepository.findById(accountId);
		return account != null ? account.getAllowance() : null;
	}
	
	public void changeAccountLocales(Parent parent, Locale locale) {
		logger.debug("Applying locale {} to {}", locale, parent);
		final Parent liveParent = enlivenParent(parent);
		final LocaleSpec localeSpec = new LocaleSpec(locale);
		
		liveParent.setDefaultLocale(localeSpec);
		parentsRepository.save(liveParent);
		
		mongoTemplate.updateMulti(Query.query(Criteria.
				where("_id").in(liveParent.getAccounts())), 
				new Update().set("locale", localeSpec), 
				Account.class);
	}

	private Parent enlivenParent(Parent parent) {
		return parentsRepository.findOne(parent.getOpenId());
	}

}

