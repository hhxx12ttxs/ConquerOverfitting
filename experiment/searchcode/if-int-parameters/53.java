import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class BankServer {
	private static int currentAcctNum = 1;
	private static ArrayList<BankAccount> accountList = new ArrayList<BankAccount>();
	private static HashMap<String,Integer> callbackClients = new HashMap<String,Integer>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Running Bank server...");
		LinkedBlockingQueue<MessageObject> inQueue = new LinkedBlockingQueue<MessageObject>();
		LinkedBlockingQueue<MessageObject> outQueue = new LinkedBlockingQueue<MessageObject>();
		
		//start UDP send and receive threads
		UUDPServer server = UUDPServer.getUDPServer(inQueue, outQueue);
		Thread serverThread=new Thread(server);
		serverThread.start();
		
		//process the requests from clients
		String name, pwd, replyString;
		Object[] parameters, reply;
		BankAccount.CurrencyEnum curr;
		float bal, amount;
		int acctNum, targetAcctNum, duration;
		MessageObject replyMessage;
		
		while(true){
			MessageObject receivedMessage = inQueue.poll();
			if(receivedMessage!=null){
				switch(receivedMessage.getOperationType()){
				
				case(1): //create account
					
					System.out.println("Command received to create new account! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
					
					name = (String) parameters[0];
					pwd = (String) parameters[1];
					curr = (BankAccount.CurrencyEnum) parameters[2];
					bal = (float) parameters[3];
					replyString = createAccount(name,pwd,curr,bal);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 1, reply);
					outQueue.add(replyMessage);
					
					break;
					
				case(2): //close account
					
					System.out.println("Command received to close account! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
				
					acctNum = (int) parameters[0];
					name = (String) parameters[1];
					pwd = (String) parameters[2];
					replyString = closeAccount(acctNum,name,pwd);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 2, reply);
					outQueue.add(replyMessage);
					
					break;
					
				case(3): //withdraw from account
					
					System.out.println("Command received to withdraw funds from account! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
				
					acctNum = (int) parameters[0];
					name = (String) parameters[1];
					pwd = (String) parameters[2];
					amount = (float) parameters[3];
					replyString = withdrawFromAccount(acctNum,name,pwd, amount);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 3, reply);
					outQueue.add(replyMessage);
					
					break;
					
				case(4): //deposit to account
					
					System.out.println("Command received to deposit to account! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
				
					acctNum = (int) parameters[0];
					name = (String) parameters[1];
					pwd = (String) parameters[2];
					amount = (float) parameters[3];
					replyString = depositToAccount(acctNum,name,pwd, amount);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 4, reply);
					outQueue.add(replyMessage);
					
					break;
					
				case(5): //check balance
					
					System.out.println("Command received to check account balance! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
				
					acctNum = (int) parameters[0];
					name = (String) parameters[1];
					pwd = (String) parameters[2];
					replyString = checkBalance(acctNum,name,pwd);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 5, reply);
					outQueue.add(replyMessage);
					
					break;
						
				case(6): //funds transfer
					
					System.out.println("Command received for funds transfer! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
				
					acctNum = (int) parameters[0];
					name = (String) parameters[1];
					pwd = (String) parameters[2];
					amount = (float) parameters[3];
					targetAcctNum = (int) parameters[4];
					replyString = fundsTransfer(acctNum,name,pwd, amount,targetAcctNum);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 6, reply);
					outQueue.add(replyMessage);
					
					break;
					
				case(7): //callback registration
					
					System.out.println("Command received to register new callback client! "+receivedMessage.getOperationParameters().toString());
					parameters = receivedMessage.getOperationParameters();
				
					duration = (int) parameters[0];
					replyString = registerNewCallbackClient(receivedMessage.getIpAddress(), receivedMessage.getPort(),duration);
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 7, reply);
					outQueue.add(replyMessage);
					
					break;
					
				default: //unrecognized input
					System.out.println("Received unrecognized operation type ("+receivedMessage.getOperationType()+")from client! "+receivedMessage.getOperationParameters().toString());
					replyString = "Error! unrecognized operation!";
					reply = new Object[1];
					reply[0] = replyString;
					
					//return reply to client
					replyMessage = new MessageObject(receivedMessage.getIpAddress(), receivedMessage.getPort(), 1, receivedMessage.getRequestID(), 7, reply);
					outQueue.add(replyMessage);
				}
			}
		}
		
		
		//add message handler
	}

	private static String registerNewCallbackClient(String ipAddress, int port,
			int duration) {
		String reply;
		String clientAddress = ipAddress + port;
		callbackClients.put(clientAddress, duration);
		
		reply = "Successfully added your client for bank monitoring!";
		return reply;
	}

	private static String fundsTransfer(int acctNum, String name, String pwd,
			float amount, int targetAcctNum) {
		String reply;
		
		BankAccount account = checkAccountExsists(acctNum);
		if(account!=null){
			if(account.getName()==name){
				if(account.checkPassword(pwd)){
					BankAccount targetAccount = checkAccountExsists(targetAcctNum);
					if(targetAccount!=null){
						//deduct amount from original account to new account
						account.setBalance(account.getBalance()-amount);
						targetAccount.setBalance(targetAccount.getBalance()+amount);
						
						reply = "Funds transfer successful! New balance for account "+account.getAccountNum()+" is "+account.getBalance();
					}else{
						reply = "Target Bank Account "+targetAcctNum+" does not exist!";
						return reply;
					}
				}else{
					//wrong password entered
					reply = "Wrong password for account "+account.getAccountNum();
					return reply;
				}
			}else{
				//wrong name (user)
				reply = "Wrong user for account "+account.getAccountNum();
				return reply;
			}
		}
		reply ="The account does not exist";
		return reply;
	}

	private static BankAccount checkAccountExsists(int acctNum){
		for(BankAccount ba: accountList){
			if(ba.getAccountNum()==acctNum)
				return ba;
		}
		return null;
	}
	
	private static String withdrawFromAccount(int accountNum, String name, String password, float amount) {
		String reply;
		
		BankAccount account = checkAccountExsists(accountNum);
		if(account!=null){
			if(account.getName()==name){
				if(account.checkPassword(password)){
					float currentBal = account.getBalance();
					account.setBalance(currentBal-amount);
					float newBal = account.getBalance();
					reply = "New balance for account "+account.getAccountNum()+" = $"+newBal;
					return reply;
				}else{
					//wrong password entered
					reply = "Wrong password for account "+account.getAccountNum();
					return reply;
				}
			}else{
				//wrong name (user)
				reply = "Wrong user for account "+account.getAccountNum();
				return reply;
			}
		}
		reply ="The account does not exist";
		return reply;
	}

	private static String depositToAccount(int accountNum, String name, String password, float amount) {
		String reply;

		BankAccount account = checkAccountExsists(accountNum);
		if(account!=null){
			if(account.getName()==name){
				if(account.checkPassword(password)){
					float currentBal = account.getBalance();
					account.setBalance(currentBal+amount);
					float newBal = account.getBalance();
					reply = "New balance for account "+account.getAccountNum()+" = $"+newBal;
					return reply;
				}else{
					//wrong password entered
					reply = "Wrong password for account "+account.getAccountNum();
					return reply;
				}
			}else{
				//wrong name (user)
				reply = "Wrong user for account "+account.getAccountNum();
				return reply;
			}
		}
		
		reply ="The account does not exist";
		return reply;
	}

	private static String closeAccount(int accountNum, String name, String password) {
		String reply;
		BankAccount account = checkAccountExsists(accountNum);
		if(account!=null){
			if(account.getName()==name){
				if(account.checkPassword(password)){
					accountList.remove(account);
					reply = "Account "+account.getAccountNum()+" successfully closed";
					return reply;
				}else{
					//wrong password entered
					reply = "Wrong password for account "+account.getAccountNum();
					return reply;
				}
			}else{
				//wrong name (user)
				reply = "Wrong user for account "+account.getAccountNum();
				return reply;
			}
		}
		
		reply ="The account does not exist";
		return reply;		
	}

	private static String createAccount(String newName, String initialPassword, BankAccount.CurrencyEnum initialCurrency, float initialBalance) {
		String reply;
		BankAccount account = new BankAccount(currentAcctNum, newName, initialPassword, initialCurrency, initialBalance);
		accountList.add(account);
		currentAcctNum++;
		reply ="The account "+account.getAccountNum()+"has been created";
		return reply;
		
		//to display account number on success
		
	}
	
	private static String checkBalance(int accountNum, String name, String password) {
		String reply;
		BankAccount account = checkAccountExsists(accountNum);
		if(account.getName()==name){
			if(account.checkPassword(password)){
				reply="Balance for account "+accountNum+" is "+account.getBalance();
			}
		}else{
			reply ="You are not an authorized user of account "+accountNum;
		}
		reply ="The account does not exist";
		return reply;
		
		//to display account number on success
		
	}

}

