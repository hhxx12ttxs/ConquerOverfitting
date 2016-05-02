package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import common.Board;
import common.Company;
import common.Coordinate;
import common.HidePasswordFromCommandLine;
import common.History;
import common.IUI;
import common.Player;
import common.Profile;
import common.SHA;
import common.Tile;
import common.enumCompany;
import common.History.historyEntry;

public class ConsoleUI implements IUI, Serializable
{
    
    /**
     * 
     */
    private static final long serialVersionUID = -6866120075442712258L;


    transient BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
    
    // Choices booleans:
    private boolean endGame = false;
    private boolean loadGame = false;
    private boolean saveGame = false;
    private boolean undoGame = false;
    private boolean redoGame = false;
    private boolean optionMade = false;
    
    // Game situations booleans:
    private boolean beginningOfTurn = false;
    private boolean unDone = false;
    private boolean localGame = false;
    private boolean firstTurn = true;
    
	private Board gameBoard;
	private Player player;
	private String[] playerNames;
	
    public ConsoleUI() {
    	player = null;
    	gameBoard = null;	
     }
	
	public void setTurnInfo(Board board,Player player) {
		this.player = player;
		this.gameBoard = board;
	}
	
	public void setTurnInfo(Player player) {
		this.player = player;
	}
	
	public void setPlayersName(String[] playerNames) {
		this.playerNames = playerNames;
	}
	
	public void setUndone(boolean unDone) {
		this.unDone = unDone;
	}
	
	public void setBeginningOfTurn(boolean beginningOfTurn) {
		this.beginningOfTurn = beginningOfTurn;
	}
	
	public void setLocalGame(boolean localGame) {
		this.localGame = localGame;
	}
	
	public void setFirstTurn() {
		firstTurn = false;
	}
	
	public boolean getEndGame() {
		return endGame;
	}
	
	public boolean getLoadGame() {
		return loadGame;
	}
	
	public boolean getSaveGame() {
		return saveGame;
	}
	
	public boolean getUndoGame() {
		return undoGame;
	}
	
	public boolean getRedoGame() {
		return redoGame;
	}
	
	public boolean getOptionMade() {
		return optionMade;
	}
	
	public void printHoF(Map<Integer,List<String>> hof)
	{
		System.out.println("HALL OF FAME");
		System.out.println("============");
		
		int i = 1;
        for (Integer result : hof.keySet())
        { 
        	int listSize = hof.get(result).size();
        	
            for (int j=0; j < listSize; j++) 
            {
                System.out.println("" + i + ". " + hof.get(result).get(j)  + "\t-\t" + result);               
                i++;
            }
        }
	}
	
    public String getPlayerName() {
        clearScreen();
        String name = null;
        
        System.out.println("Player, please enter your name:");
        try {
			name = buffer.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return name;
    }
    
    public String getPlayerName(int i) {
        clearScreen();
        String name = null;
        
        System.out.println("Player "+i+", please enter your name:");
        try {
			name = buffer.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return name;
    }

    private String getUserInput()
    {
        String input = null;
        
        try
        {                
            input = buffer.readLine();
            if (input.equals("info"))
            {
                if (player == null)
                {
                    System.out.println("Does not compute");
                }
                else 
                {
                    printInfo();
                }
            }
            
            else if (input.equals("help") || input.equals("infocard"))
            {
            	printTextFile("resources/" + input + ".aqr");
            }            
            
            else if (input.equals("board"))
            {
            	printBoard();            	
            }  
        } 
        
        catch (IOException e) 
        {
             e.printStackTrace();
        }
        
        return (input);  
    }
    
    private static int safeParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void printBoard() {
    	clearScreen();
    	System.out.println("Game Board:");
    	System.out.println();
        System.out.print(" ");
        for (int j = 1; j <= 12; j++) {
            if (j==10) {
                System.out.print(" ");
            }
            if (j<10) {
                System.out.print("   " + j );
            }
            else {
                System.out.print("  " + j );    
            }

        }
        System.out.println();
    	System.out.println("   -----------------------------------------------");
    	for (int i = 0; i < 9; i++) {
    		System.out.print((char)(i+65) + " ");
    	    for (int j = 0; j < 12; j++) {
        		System.out.print("|");        		
        		System.out.print(" " + gameBoard.getTile(i,j).getInitial() + " ");
    		}
        	System.out.println("|");
        	System.out.println("   -----------------------------------------------");
    	}  
    }
    

    public void printError(int errorIndex) {
    	switch(errorIndex)
    	{
    	case 1:
    		System.err.println("Server in not connected");
    		break;
    	case 2:
    		System.err.println("Remote connection error");
    		break;
    	case 3:
    		
    		break;
    	case 4:
    		
    		break;
    	case 5:
    		
    		break;
    	}

    }

    public void printInfo() {
        int companyStocks;
        
        System.out.println("Money: " + player.getCash());
        System.out.println();
        System.out.println("Your hand: ");
        for (Tile tile : player.getTiles()) {
            System.out.print(tile + " ");
        }
        System.out.println();
        
        for (int i=0; i<7; i++) {
            companyStocks = player.getCompanyStocks(enumCompany.values()[i]);
            System.out.println(enumCompany.values()[i] + ": " + companyStocks + " stocks");
        }
        System.out.println();
        
    }
    
//    public void pickTileDialogText(int tilesPerPlayer) {
//    	int counter = 0;
//    	List<Tile> tiles = player.getTiles();
//        System.out.println(player + ", it's your turn.");
//        System.out.println("Enter the tile you wish to place on board");      
//        for (Tile tile : tiles) {
//            counter++;
//            System.out.println("Tile " + counter + " :" + tile);
//        }
//        System.out.println("Please enter a number between 1-" + tilesPerPlayer+ ": ");
//    }

    public Coordinate printPickTileDialog() {
        
        int counter = 0, num = -1;
        List<Tile> tiles = player.getTiles();
        int playerTilesCount = player.getTiles().size();
        String userInput = null;
        clearScreen();
        
        System.out.println(player + ", it's your turn.");
        System.out.println("Enter the tile you wish to place on board"); 
        
        for (Tile tile : tiles) {
            counter++;
            System.out.println("Tile " + counter + " :" + tile);
        }
        
        while (num < 1 || num > playerTilesCount) {
            System.out.println("Please enter a number between 1-" + playerTilesCount+ ": ");
            userInput = getUserInput();
            pickOption(userInput);
            
            // check if an option was picked:
            if (getOptionMade()) {
            	return null;	
            }
            
            
            tiles = player.getTiles();   //Important for loading/undoing/redoing
            num = safeParseInt(userInput);
        }

        return tiles.get(num-1).getLocation();
    }
    
    public boolean pickOption(String input) {
        if (input.equalsIgnoreCase("quit")) {
        	if (!beginningOfTurn) {
        		System.out.println("Can only quit at the beginning of the turn");
        		return false;
        	}
        	optionMade = true;
        	endGame = true;
        	return true;
        }
        else if (input.equalsIgnoreCase("load")) {
        	if (!localGame) {
        		System.out.println("Can only load in local game");
        		return false;
        	}
        	if (!beginningOfTurn) {
        		System.out.println("Can only load at the beginning of the turn");
        		return false;
        	}
        	optionMade = true;
        	loadGame = true;
        	return true;
        }
        else if (input.equalsIgnoreCase("save")) {
        	if (!localGame) {
        		System.out.println("Can only save in local game");
        		return false;
        	}
        	if (!beginningOfTurn) {
        		System.out.println("Can only save at the beginning of the turn");
        		return false;
        	}
        	optionMade = true;
        	saveGame = true;
        	return true;
        }
        else if (input.equalsIgnoreCase("undo")) {
        	if (!localGame) {
        		System.out.println("Can only undo in local game");
        		return false;
        	}
        	if (firstTurn) {
        		System.out.println("Can't undo at the first turn of the game");
        		return false;
        	}
        	if (!beginningOfTurn) {
        		System.out.println("Can only undo at the beginning of the turn");
        		return false;
        	}
        	if (unDone) {
        		System.out.println("Can't undo more the once");
        		return false;
        	}
        	optionMade = true;
        	undoGame = true;
        	return true;
        }
        else if (input.equalsIgnoreCase("redo")) {
        	if (!localGame) {
        		System.out.println("Can only redo in local game");
        		return false;
        	}
        	if (!beginningOfTurn) {
        		System.out.println("Can only redo at the beginning of the turn");
        		return false;
        	}
        	if (!unDone) {
        		System.out.println("Can't redo without doing undo before");
        		return false;
        	}
        	optionMade = true;
        	redoGame = true;
        	return true;
        }
        else
        	return false;
    }
    
    public void resetOptions() {
    	optionMade = false;
    	loadGame = false;
    	saveGame = false;
    	undoGame = false;
    	redoGame = false;	
    }
        
        
     
/*    public void printSellCompanyDialog(Company soldCompany, Company buyingCompany, List<Player> topShareHolders, 
            List<Player> secondShareHolders, int prizeMoney, int secondprizeMoney, Player[] players) {
        
        ShareSizeComparator comparator = new ShareSizeComparator(enumCompany.values()[soldCompany.getCompanyIndex()]);
      
        for(Player player : players){
        	clearScreen();
        
        	if (buyingCompany == null) {
        		player.getUI().printMessage(16, soldCompany.getCompanyIndex());
        	} else {
        		player.getUI().printMessage(17, soldCompany.getCompanyIndex(), buyingCompany.getCompanyIndex());
        	}
         
        	player.getUI().printMessage(18);
        	for (Player shareHolder : topShareHolders) {
        		player.getUI().printMessage(19, shareHolder.getIndex(), prizeMoney);
        	}
        
        	if (secondShareHolders.size() > 0) {
        		player.getUI().printMessage(20);  
        		for (Player shareHolder : secondShareHolders) {
        			player.getUI().printMessage(21, shareHolder.getIndex(),secondprizeMoney);
        		}
        	}
    	}
        int counter = 0;
        for (int i=0; i<players.length; i++) {
            if (players[i].getCompanyStocks(enumCompany.values()[soldCompany.getCompanyIndex()]) > 0) {
                counter++;
            }
        }
        
        Player shareHolders[] = new Player[counter];
        
        int j = 0;
        for (int i=0; i<players.length; i++) {
            if (players[i].getCompanyStocks(enumCompany.values()[soldCompany.getCompanyIndex()]) > 0) {
                shareHolders[j] = players[i];
                j++;
            }
        }
        
        Arrays.sort(shareHolders, comparator);
        
        for (Player shareHolder : shareHolders) {
        	shareHolder.getUI().printMessage(22);
            System.out.println(player.getIndex() + ", you have " + 
                    player.getCompanyStocks(enumCompany.values()[soldCompany.getCompanyIndex()]) +
                    " stocks of " + soldCompany);
        }
        
        System.out.println();
        
    }*/
    
    public int printSellCompanySell(Company soldCompany) {
        String userInput;
        int toSell = 0;
        
        do
        {
	        clearScreen();        
	        
	        System.out.println("how many of your sold company stocks would you like to sell?");
	        userInput = getUserInput();
	        toSell = safeParseInt(userInput);
        }
        while(toSell == -1);
        
        return toSell;
    }
    
    
    public int printSellCompanyChange(Company soldCompany, Company buyingCompany) {
       String userInput;
       int toChange = 0;
       int stocksLeft = buyingCompany.remainingStocksInBank();
       
       do
       {
	       clearScreen();       
	       
	       System.out.println (buyingCompany.toString()+" has "+stocksLeft+" stocks left");
	       System.out.println("how many of your sold company stocks would you like to convert?");
	       userInput = getUserInput();
	       toChange = safeParseInt(userInput);
	   }
	   while(toChange == -1);
       
       return toChange;
   }
    
   
    public int[] printStocksToBuyDialog(Company[] companies) {
        clearScreen();        
    	
        int[]  stocks= new int[7];
        int toBuy = -1,choice = 0, count=0;

        System.out.println("the following are the companies on board:");
        for (Company c : companies){
           count++;
           if (c.isOnBoard())
System.out.println("Company "+count+ ": "+c.toString());
        }
        count = 0;
        
        System.out.println("how many stocks would you like to buy?");
        System.out.println("please enter a number between 0-3.");
         toBuy = getNumberChoice(0, 3);
        if (toBuy == 0) return stocks;
        System.out.println("the following are the companies on board:");
        for (Company c : companies){
    	   count++;
    	   if (c.isOnBoard())
    		   System.out.println("Company "+count+ ": "+c.toString());
    	}
        
        for (int i = 0; i < toBuy; i++) {
        	choice = 0;
            System.out.println("please choose a company to buy 1 stock from:");
            System.out.println("Please enter a number between 1-7");
            choice = getNumberChoice(1, 7);
        	while (!companies[choice-1].isOnBoard()){
        		choice = 0;
        	    System.out.println("please choose a company to buy 1 stock from, NOTICE THAT IT HAS TO BE ON BOARD!");
        	    choice = getNumberChoice(1, 7);
        	}
            stocks[choice-1]++;
        }
        return stocks;
     }
        
        
     public void clearScreen() {
//    	for (int i=0;i<24;i++)
//        System.out.println("\n");
       System.out.println();
    }

	
     
	public enumCompany printChooseCompanyDialog(Company[] companies) {
           int count = 0,choice = 0;
         
           clearScreen();
        
      	    System.out.println("the following are the companies available to establish:");
      	    for (Company c : companies){
      	    	count++;
      	    	if (!c.isOnBoard())
      	    	System.out.println("Company "+count+ ": "+c.toString());
       	}
      	
      	    System.out.println("please choose a company to establish:");
      	    choice = getNumberChoice(1, 7);
             
		return enumCompany.values()[choice-1];
	}
    
     
     public void printMessage(String msg) {
         System.out.println(msg);
     }
     
     
    public void printMessage(int msg,int ...args){
    	switch(msg)
    	{
    	case 1:
    		System.out.println("You have no legal moves to make! Your turn will be skipped, but you may buy stocks.\n");
    		break;
    	case 2:
    		System.out.println("Notice: The tile you've placed is in between two safe companies.\nChoose one more tile to place.\n");
    		break;
    	case 3:
    		System.out.println("No free stock is given since no stocks of the company are left in the bank\n");
    		break;
    	case 4:
    		System.out.println("Couldn't fetch a new tile - There are no more tiles in the bank!\n");
    		break;
    	case 5:
    		System.out.println("Transaction Completed.\n" + args[0] + " bucks were withdrawn from your bank account.\n");
    		break;
    	case 6:
    		System.out.println("You don't have enough money - you are " + args[0] + " bucks short\n");
    		break;
    	case 7:
    		System.out.println("You must pick a number between 0 and " + args[0] + "\n");
    		break;
    	case 8:
    		System.out.println("You must pick an even number between 0 and " + args[0]);
    		break;
    	case 9:
    		System.out.println("Waiting for other players...");
    		break;
    	case 10:
    		System.out.println("There are only " + args[0] + " stocks of " + enumCompany.values()[args[1]].toString() + " left to buy\n");
    		break;
    	case 11:
    		System.out.println(playerNames[args[0]] + " sold " + args[1] + " shares for " + args[2] + " bucks\n");
    		break;
    	case 12:
    		System.out.println(playerNames[args[0]] + " converted " + args[1] + " shares into " + args[2] + " " + enumCompany.values()[args[3]].toString() + " shares\n");
    		break;
    	case 13:
    		System.out.println(playerNames[args[0]]+ " held on to " + args[1] + " shares\n");
    		break;
    	case 14:
    		System.out.println(playerNames[args[0]] + " sold " + args[1] + " for " + args[2] + " bucks\n");
    		break;
    	case 15:
    		System.out.println("Error: Can't place a tile which creates a company when all companies are on board.\nRe-pick a tile.");
    		break;
    	case 16:
    		System.out.println("Company " + enumCompany.values()[args[0]].toString() + " is sold to the bank");
    		break;
    	case 17:
    		System.out.println("Company " + enumCompany.values()[args[0]].toString() + " is sold to company " + enumCompany.values()[args[1]].toString());
    		break;
    	case 18:
    		System.out.println("First Prize Wins:");
    		break;
    	case 19:
    		System.out.println(playerNames[args[0]] + " gets $" + args[1]);
    		break;
    	case 20:
    		System.out.println("Second Prize Wins:"); //ODED YOU ARE HERE
    		break;
    	case 21:
    		System.out.println(playerNames[args[0]] + " gets $" + args[1] +"\n");
    		break;
    	case 22:
    		System.out.println("Share Holders:");
    		break;
    	case 23:
    		System.out.println(playerNames[args[0]] + ", has " + args[1] + " stocks of " + 
    				 enumCompany.values()[args[2]].toString());
    		System.out.println();
    		break;
    	case 24:
    		System.out.println("No rooms yet, creating a new room.");
    		break;	
    	case 25:
    	    System.out.println("Chosen name already exists, please choose a different name");
    	    break;
    	case 26:
    	    System.out.println();
    	    System.out.println("***********  ROOMS LIST  ************");
    	    System.out.println();
    	    break;
    	case 27:
    	    System.out.print("Connecting... Please wait.");
    	    break;
    	case 28:
    	    System.out.println(" Just a little longer...");
    	    break;
    	case 29:
    	    System.out.println(playerNames[args[0]]+", its your turn:");
    	    System.out.println();
    	    break;
    	case 30:
    		System.out.println("it is "+playerNames[args[0]]+"'s turn.");
    		break;
    	case 31:
    		System.out.println(playerNames[args[0]]+" just bought "+ args[1] +" stocks of "+ enumCompany.values()[args[2]].toString());	
    		break;
    	case 32:
    		System.out.println(playerNames[args[0]]+ " placed tile " +((char)(args[1]+65))+""+(args[2]+1));
    	    break;
    	case 33:
    		System.out.println(playerNames[args[0]]+ " didn't have any tile to place on board");
    		break;
    	case 34:
    		System.out.println(playerNames[args[0]]+ " didn't buy any stocks");
    		break;
    	case 35:
    		System.out.println("and also established company "+ enumCompany.values()[args[0]].toString());
    		break;
    	case 36:
    		System.out.println("Your current rating is: "+args[0]);
    		break;
    	case 37:
    		System.out.println("Changes succeed");
    		break;
    	case 38:
    		System.out.println("Bye bye");
    		break;
    	} 
    	    
    }
    
    public String changePasswordDialog(String oldPassword) {
    	try {
    		int tryNum = 3;
    		for (int i = 0;i<tryNum;i++) {
    			System.out.print("Enter your old password: ");
    			String oldPasswordTyped;
    			oldPasswordTyped = buffer.readLine();
    			oldPasswordTyped = SHA.SHA1(oldPasswordTyped);
    			if (oldPassword.compareTo(oldPasswordTyped)!=0) {
    				System.out.println("Old password incorrect.");
    				System.out.println();
    				continue;
    			}
    			System.out.println("Enter your new password: ");	
    			String newPasswordTyped = buffer.readLine();
    			System.out.println("Reconfirm your new password: ");	
    			String newPasswordConfirmation = buffer.readLine();
    			if (newPasswordTyped.compareTo(newPasswordConfirmation)!=0) {
    				System.out.println("Reconfirmation of your new password was incorrect.");
    				System.out.println();
    				continue;
    			}
    			newPasswordTyped = SHA.SHA1(newPasswordTyped);
    			return newPasswordTyped;	
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
    }
    
    public String changeEmailDialog() {
    	System.out.print("Enter your new Email address: ");
    	String newEmailTyped = null;
		try {
			newEmailTyped = buffer.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return newEmailTyped;
    }
    
    public void printEndGameResults(Map<Integer, List<Player>> finalStandings) {
        int i = 1;
        System.out.println();
        for (Integer result : finalStandings.keySet()) { 
            while (!finalStandings.get(result).isEmpty()) {
                System.out.println("" + i + ". " + finalStandings.get(result).get(0)  + "\t-\t" + result);
                finalStandings.get(result).remove(0);
                i++;
            }
            
        }
        System.out.println();
    }
    
    public void printTextFile(String FileName) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("/" + FileName);
        
        // we do this hack to allow both reading from a file and reading from inside the client jar
        if (in == null) {
            try {
                in = new FileInputStream(FileName);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        String message;
        try {
            while (( message = buffer.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	public void printSortBetweenSameSizeCompanies(List<Company> sameSizedCompanies, int size)
	{
		String userInput;		
		int choice;
		List<Company> listCopy = new ArrayList<Company>(sameSizedCompanies);
		
		sameSizedCompanies.clear();
		
		System.out.println("The following companies are all of size " + size + ".\nPlease choose the company to be sold first:");
		
		while (listCopy.size() >= 2)
		{
			if (sameSizedCompanies.size() > 0)
			{
				//happens when there are more than 2 companies of the same size..
				System.out.println("please choose the next company to be sold:");
			}
			
			int num = 0;
			for (Company company : listCopy)
			{
				System.out.println(num + ": " + company.toString());
				num++;
			}
			
	
			do
			{
			    userInput = getUserInput();
			    choice = safeParseInt(userInput);	           
			}
			while(choice < 0 || choice > listCopy.size()-1);	   

			sameSizedCompanies.add(listCopy.get(choice));
			listCopy.remove(choice);
		}		
		
		sameSizedCompanies.add(listCopy.get(0)); //adding the last company
	}

	public int getNumberOfPlayers() {
        clearScreen();
        System.out.println("Please enter number of players for the game: ");
        return getNumberChoice(2, 6);
	}

	public int getUserSetupDecision(boolean guest) {
        clearScreen();
        System.out.println("Would you like to: ");
        System.out.println("(1) Create new game");
        System.out.println("(2) Join existing game");
        System.out.println("(3) Play VS computer");
        if (!guest) {
        	System.out.println("(4) Check your profile");
        	System.out.println("(5) Quit");
        } else {
        	System.out.println("(4) Quit");
        }
        System.out.println();
        if (guest) {
        	int choise =  getNumberChoice(1, 4);
        	if (choise == 4) choise++;
        	return choise;
        } else {
        	return getNumberChoice(1, 5);
        }
	}	

	
	public boolean closeExportedObject() {
	    try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException e) {
            return false;
        }
        return true;
	}
	
	private int getNumberChoice(int low,int high) {
		int num;
		while (true) {
		    System.out.println("Enter the number of your choice:");
		    String userInput = getUserInput();
		    num = safeParseInt(userInput);
		    if ((num >= low) && (num <= high))
		        break;
		    else {
		        System.out.println("Must enter a number between "+low+"-"+high);
		        System.out.println();
		    }
		}
		return num;
	}
	
	public int getUserLoginDecision() {
	    clearScreen();
	    System.out.println("Would you like to: ");
	    System.out.println("(1) Create a new account");
	    System.out.println("(2) Login using existing account");
	    System.out.println("(3) Play as guest");
	    System.out.println();
	    return getNumberChoice(1, 3);
	}
	
	public int getUserRoomDecision(int N) {
		System.out.println("Which room would you like to join: ");
		 return getNumberChoice(1, N);
	}
	
	public int getUserGameTypeDecision() {
        clearScreen();
        System.out.println("Would you like to: ");
        System.out.println("(1) Play local game");
        System.out.println("(2) Play online game");
        System.out.println("(3) Quit");
        System.out.println();
        return getNumberChoice(1, 3);
	}

	public int getHostAddressDecision() {
        clearScreen();
        System.out.println("Would you like to connect through: ");
        System.out.println("(1) Local host");
        System.out.println("(2) Delta-tomcat-vm");
        System.out.println();
        return getNumberChoice(1, 2);
	}
	
	public int getPortDecision() {
        clearScreen();
        System.out.println("Would you like to: ");
        System.out.println("(1) Use random port");
        System.out.println("(2) Choose a port");
        System.out.println();
        int decision = getNumberChoice(1, 2);
        if (decision == 1) {
        	return -1;
        } else {
        	System.out.println("Enter the port you want to use:");
        	return getNumberChoice(0, 65535);
        }
	}
	
	public int getUserProfileDecision() {
        clearScreen();
        System.out.println("Would you like to: ");
        System.out.println("(1) Change password");
        System.out.println("(2) Change Email address");
        System.out.println("(3) View game history");
        System.out.println("(4) View current rating");
        System.out.println("(5) Return");
        System.out.println();
        return getNumberChoice(1, 5);
	}

	public Profile getRegistrationDetails() {
	    
	    Profile newProfile = null;
	    
	    try {
    	    System.out.print("Enter username: ");
	        String username = buffer.readLine();
    	    System.out.print("Enter password: ");
    	    
    	    HidePasswordFromCommandLine hideThread = new HidePasswordFromCommandLine();
            //hideThread.start();         
            String password = buffer.readLine();            
            hideThread.stopThread = true;
    	    
    	    password = SHA.SHA1(password);
    	    System.out.print("Enter email address: ");
    	    String email = buffer.readLine();
    	    newProfile = new Profile(username, password, email);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return newProfile;
	}
	
	public String[] getLoginDetails(int tryNum) {
	    String[] details = new String[2];
	    
	    if (tryNum != 0)
	    {
	        System.out.println("Login Failed. " + (3-tryNum) + " tries left");	       
	    }
	        
	    try {
    	    System.out.print("Enter your username: ");
    	    String username = buffer.readLine();
    	    System.out.print("Enter your password: ");
    	    
    	    HidePasswordFromCommandLine hideThread = new HidePasswordFromCommandLine();
            //hideThread.start();         
            String password = buffer.readLine();            
            hideThread.stopThread = true;
    	    
    	    password = SHA.SHA1(password);
    	    details[0] = username;
    	    details[1] = password;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return details;
	}
	
	public void printHistory(History history) {
		System.out.println("******************************************************");
		System.out.println("******************  Game History   *******************");
		System.out.println("******************************************************");
		System.out.println();
		for (int i=0;i<history.getNumOfHistories();i++) {
			printHistoryEntry(history.getEntry(i),i+1);
			System.out.println();
		}
		System.out.println("******************************************************");
	}
	
	private void printHistoryEntry(historyEntry entry,int j) {
		System.out.println(j+".\tDate: "+entry.getDate());
		System.out.println("\tOpponents:");
		for (int i=1;i<=entry.getRivals().size();i++) {
			System.out.println("\t   "+i+". "+entry.getRivals().get(i-1));
		}
		System.out.println("\tChange in Rating: "+entry.getScore());
	}
	
// Local Version:
	private String getFileName() {
		String name = null;
		try {
			name = buffer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return name;
	}

	public boolean getYesNoQuestionAnswer() {
		String answer = null;
		try {
			answer = buffer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return answer.equals("y");
	}

	public String getSaveFileName() {
		System.out.println("Please enter a name for your save file:");
		String saveFileName = getFileName();
		saveFileName.concat(".aqr");
		File f = new File(saveFileName);
		if (f.exists()) {
			System.out.println("File already exists, overwrite?");
			System.out.println("type y for YES and any other key for NO");
			if (!getYesNoQuestionAnswer())
				return null;
		}
		return saveFileName;	 	 
	}

	public String getLoadFileName() {
		System.out.println("Please enter load file name:");
		String loadFileName = getFileName();
		loadFileName.concat(".aqr");
		File f = new File(loadFileName);
		if (!f.exists()) {
			System.out.println("File not Exists");
			return null;
		}
		return loadFileName;		 
	}

}

/*public int[] getUserGameDefinitions()
{
	int num=0;
	String userInput;
	int[] definitions = new int[2];
	
	do
	{
		System.out.println("how much money each player should start with?");
        
		userInput = getUserInput();
		num = safeParseInt(userInput);
        
	}
	while (num < 0);
	
	definitions[0] = num;
	
	do
	{
		System.out.println("how many tiles each player should have (10 tops)?");
		userInput = getUserInput();
		num = safeParseInt(userInput);
	}
	while (num < 1 || num > 10);
	
	definitions[1] = num;
	
	return definitions;
}*/
