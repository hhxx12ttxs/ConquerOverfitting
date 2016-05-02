package crawlerAlpha;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;


public class Manager extends JApplet implements Runnable, MouseListener{

	Thread getStarted, loadingThread;

	int xsize = 1000;
	int ysize = 700;
	int sleeptime = 20;
	int pagenum = 0;
	int gameselected = -1;

	double totalvalue = 0;

	Graphics bufferGraphics;
	Image bufferImage;
	Container applet;
	
	Connector connector;

	Image dialogoverlay;
	Image selectionbar;

	Button b0 = new Button("Login");
	Button b1 = new Button("Add Game");
	Button b2 = new Button("Update");
	Button b3 = new Button("Logout");
	Button b4 = new Button("Title");
	Button b5 = new Button("System");
	Button b6 = new Button("Used Value");
	Button b7 = new Button("Delete Game");
	Button np = new Button("-->");
	Button pp = new Button("<--");

	TextField username = new TextField(50);
	TextField password = new TextField(50);    
	TextField gametitle = new TextField(50);
	Button submitGT = new Button("Submit");
	Button submitUN = new Button("Login");
	Button cancel = new Button("Cancel");
	
	Button xbox360 = new Button("Xbox 360");
	Button ps3 = new Button("PS3");
	Button wii = new Button("Wii");
	Button pc = new Button("PC");
	Button ps2 = new Button("PS2");
	Button xbox = new Button("Xbox");
	Button gamecube = new Button("Gamecube");
	Button ps1 = new Button("PS1");
	Button n64 = new Button("N64");
	Button snes = new Button("SNES");
	Button nes = new Button("NES");
	Button dreamcast = new Button("Dreamcast");
	Button genesis = new Button("Genesis");
	Button psp = new Button("PSP");
	Button ds = new Button("DS");
	Button other = new Button("Other");
	
	Button correct = new Button("Yes");
	Button incorrect = new Button("No");
	Button s1 = new Button("Game Option 1");
	Button s2 = new Button("Game Option 2");
	Button s3 = new Button("Game Option 3");
	Button s4 = new Button("Game Option 4");
	Button s5 = new Button("Game Option 5");
	Button tryagain = new Button("Try Again");

	boolean userisloggedin = false;
	boolean userloginwrong = false;
	boolean connectedtoserver = false;
	boolean indialog = false;
	boolean singlechoice = false;
	boolean multichoice = false;
	boolean incorrectgame = false;

	String title = "Game Manager";
	String build = "Pre-Alpha GUI Build";
	String userid;
	String pass;

	String gameTitle;
	String console;

	Font f1 = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
	Font f2 = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
	Font f3 = new Font(Font.SANS_SERIF, Font.BOLD, 15);

	/*
	 * The follow is the array the holds the Game Objects
	 * Very Important!
	 */

	Game gamearray[];
	Game gamechoicearray[];
	Game gametoadd;


	public void init(){
		
		this.setSize(xsize, ysize);

		/*
		 * Creates the buffered image for paint()
		 */
		bufferImage = createImage(xsize, ysize);
		bufferGraphics = bufferImage.getGraphics();

		connector = new Connector();
		
		connectedtoserver = connector.checkServerConnection();

		/*
		 * Sets up initial buttons
		 */
		applet = getContentPane();
		applet.setLayout(null);
		applet.add(b0);
		b0.setBounds(490, 15, 100, 25);
		b0.setEnabled(true);
		applet.add(b1);
		b1.setBounds(600, 15, 100, 25);
		b1.setEnabled(false);
		applet.add(b2);
		b2.setBounds(710, 15, 100, 25);
		b2.setEnabled(false);
		applet.add(b4);
		b4.setBounds(0, 50, 400, 20);
		b4.setEnabled(false);
		applet.add(b5);
		b5.setBounds(400, 50, 400, 20);
		b5.setEnabled(false);
		applet.add(b6);
		b6.setBounds(800, 50, 200, 20);
		b6.setEnabled(false);  
		applet.add(np);
		np.setBounds(980, 0, 20, 50);
		np.setEnabled(false);
		applet.add(pp);
		pp.setBounds(960, 0, 20, 50);
		pp.setEnabled(false);
					
		addMouseListener(this);
				

	}

	public void update(Graphics g){

		paint(g);

	}

	public void paint(Graphics g){

		/*
		 * Draw the Titles, Menu Bars, and Backgrounds
		 */
		bufferGraphics.setColor(Color.white);
		bufferGraphics.fillRect(0, 0, xsize, ysize);
		bufferGraphics.setColor(Color.black);
		bufferGraphics.drawLine(399, 0, 399, 700);
		bufferGraphics.drawLine(799, 0, 799, 700);
		bufferGraphics.setColor(new Color(150, 180, 190));
		bufferGraphics.fillRect(0, 0, xsize, 50);
		bufferGraphics.fillRect(0, (ysize - 25), xsize, 25);
		bufferGraphics.setFont(f1);
		bufferGraphics.setColor(new Color(255, 255, 255));
		bufferGraphics.drawString(title, 10, 30);
		bufferGraphics.setFont(f2);
		bufferGraphics.drawString(build, 175, 30);
		bufferGraphics.setFont(f3);

		/*
		 * Draws Server Connection Status
		 */
		if(connectedtoserver == false){
			bufferGraphics.setColor(Color.red);
			bufferGraphics.drawString("Not Connected to Server", 5, 692);
		}else{
			bufferGraphics.setColor(Color.green);
			bufferGraphics.drawString("Connected to Server", 5, 692);
		}

		/*
		 * If user is in Dialog, draws text and background for dialogs
		 */
		if(indialog == true){
			bufferGraphics.setFont(f1);
			bufferGraphics.setColor(Color.white);
			bufferGraphics.drawImage(dialogoverlay, 0, 50, this);

			if(gametitle.isShowing()){
				bufferGraphics.drawString("Enter Game Title", 410, 330);
			}else if(username.isShowing()){
				bufferGraphics.drawString("Please Login", 20, 100);
				bufferGraphics.drawString("Username", 310, 370);
				bufferGraphics.drawString("Password", 310, 400);
				if(userloginwrong == true){
					bufferGraphics.setColor(Color.red);
					bufferGraphics.drawString("Login Incorrect", 410, 330);
					bufferGraphics.setColor(Color.white);
				}
			}else if(singlechoice == true){
				bufferGraphics.drawString("Is this game correct?", 400, 200);
				bufferGraphics.drawString(gametoadd.getTitle(), 250, 300);
				bufferGraphics.drawString("for", 350, 300);
				bufferGraphics.drawString(gametoadd.getSystem(), 450, 300);
				bufferGraphics.drawString("valued at", 550, 300);
				bufferGraphics.drawString("$" + gametoadd.getUsedValue(), 650, 300);
				
			}else if(multichoice == true){
				bufferGraphics.drawString("Please select your game:", 300, 200);
			}else if(incorrectgame == true){
				bufferGraphics.setColor(Color.red);
				bufferGraphics.drawString("Cannot Find Game", 300, 200);
				bufferGraphics.drawString("Try the following: ", 350, 230);
				bufferGraphics.drawString("-Check your game title", 400, 300);
				bufferGraphics.drawString("-Check your game system", 400, 350);
			}

		}

		/*
		 * If the user is logged in, draws user name and then draws game list
		 */
		if(userisloggedin == true && indialog == false){

			bufferGraphics.setFont(f2);
			bufferGraphics.setColor(Color.white);
			bufferGraphics.drawString("Total Value: $" + totalvalue, 805, 692);
			bufferGraphics.drawString("User: " + userid, 850, 30);

			/*
			 * If the game array isn't empty, draw the games
			 */
			if(gamearray[0] != null){
				drawGames();
			}

			if(gameselected >= 0 && gameselected < gamearray.length){
				int drawselected = gameselected - (pagenum * 20);
				bufferGraphics.drawImage(selectionbar, 0, (drawselected * 30) + 75, this);

			}

		}

		g.drawImage(bufferImage, 0, 0, null);
	}

	private void drawGames(){
		/*
		 * This method lists the games in the user's database
		 * It also creates invisible buttons for game selection
		 */

		int placement = 0;
		int index = 0;
		if(pagenum > 0){
			index = index + (pagenum * 20);
		}
		int initialindex = index;
		bufferGraphics.setColor(Color.black);
		bufferGraphics.setFont(f2);

		while(index < gamearray.length && index - initialindex != 20){
			if(gamearray[index] != null){
				bufferGraphics.drawString(gamearray[index].getTitle(), 10, ((placement *30) + 90));
				bufferGraphics.drawString(gamearray[index].getSystem(), 410, ((placement * 30) + 90));
				bufferGraphics.drawString("" + gamearray[index].getUsedValue(), 810, ((placement * 30) + 90));
			}
			index++;
			placement++;
		}


	}	

	private void addGame(){

		/*
		 * This method starts the add game process
		 * It opens the dialogs and disables buttons
		 */

		indialog = true;

		b1.setEnabled(false);
		b2.setEnabled(false);
		b4.setEnabled(false);
		b5.setEnabled(false);
		b6.setEnabled(false);
		pp.setEnabled(false);
		np.setEnabled(false);
		applet.remove(b7);
		gameselected = -1;

		applet.add(gametitle);
		gametitle.setBounds(410, 350, 200, 25);
		gametitle.setEnabled(true);
		applet.add(submitGT);
		submitGT.setBounds(630, 350, 100, 25);
		submitGT.setEnabled(true);
		applet.add(cancel);
		cancel.setBounds(630, 380, 100, 25);
		cancel.setEnabled(true);

	}
	
	private void selectConsole(){
		
		/*
		 * This method displays list of consoles for user to pick one
		 */
		applet.remove(gametitle);
		applet.remove(submitGT);
		applet.add(xbox360);
		xbox360.setBounds(490, 200, 100, 25);
		xbox360.setEnabled(true);
		applet.add(ps3);
		ps3.setBounds(380, 200, 100, 25);
		ps3.setEnabled(true);
		applet.add(wii);
		wii.setBounds(600, 200, 100, 25);
		wii.setEnabled(true);
		applet.add(ps2);
		ps2.setBounds(380, 235, 100, 25);
		ps2.setEnabled(true);
		applet.add(xbox);
		xbox.setBounds(490, 235, 100, 25);
		xbox.setEnabled(true);
		applet.add(gamecube);
		gamecube.setBounds(600, 235, 100, 25);
		gamecube.setEnabled(true);
		applet.add(ps1);
		ps1.setBounds(380, 270, 100, 25);
		ps1.setEnabled(true);
		applet.add(dreamcast);
		dreamcast.setBounds(490, 270, 100, 25);
		dreamcast.setEnabled(true);
		applet.add(n64);
		n64.setBounds(600, 270, 100, 25);
		n64.setEnabled(true);
		applet.add(pc);
		pc.setBounds(380, 305, 100, 25);
		pc.setEnabled(true);
		applet.add(genesis);
		genesis.setBounds(490, 305, 100, 25);
		genesis.setEnabled(true);
		applet.add(snes);
		snes.setBounds(600, 305, 100, 25);
		snes.setEnabled(true);
		applet.add(nes);
		nes.setBounds(600, 340, 100, 25);
		nes.setEnabled(true);
		applet.add(psp);
		psp.setBounds(380, 340, 100, 25);
		psp.setEnabled(true);
		applet.add(ds);
		ds.setBounds(490, 340, 100, 25);
		ds.setEnabled(true);
		applet.add(other);
		other.setBounds(490, 375, 100, 25);
		other.setEnabled(true);		
	}
	
	private void hideConsoles(){
		applet.remove(xbox360);
		applet.remove(xbox);
		applet.remove(ps3);
		applet.remove(ps2);
		applet.remove(ps1);
		applet.remove(psp);
		applet.remove(dreamcast);
		applet.remove(genesis);
		applet.remove(wii);
		applet.remove(gamecube);
		applet.remove(n64);
		applet.remove(snes);
		applet.remove(nes);
		applet.remove(ds);
		applet.remove(pc);
		applet.remove(other);
		applet.remove(cancel);
	}
	
	private void hideGameSelections(){
		multichoice = false;
		applet.remove(s1);
		applet.remove(s2);
		applet.remove(s3);
		applet.remove(s4);
		applet.remove(s5);
		
	}

	private void checkGame(){

		/*
		 * This method will take the game title and send it to the server
		 * The server will check the title and return results
		 */
		
		boolean check = false;
		
		check = connector.checkGame(gameTitle, console);
		
		displayResult(check);

	}
	
	private void displayResult(boolean check){
		
		if(check == true){
			gametoadd = connector.getGame(gameTitle, console);
			singlechoice = true;
			applet.add(correct);
			applet.add(incorrect);
			correct.setBounds(200, 400, 100, 25);
			incorrect.setBounds(500, 400, 100, 25);
			applet.add(cancel);
			cancel.setBounds(800, 400, 100, 25);
		}else{
			displayGameChoices();
		}
		
	}
	
	private void displayGameChoices(){
		
		gamechoicearray = connector.findGame(gameTitle, console);
		multichoice = true;
		if(gamechoicearray.length >= 1){
			applet.add(s1);
			s1.setBounds(200, 250, 600, 50);
			s1.setLabel(gamechoicearray[0].getTitle()+ "        on         " + gamechoicearray[0].getSystem()+ "   valued at   $" + gamechoicearray[0].getUsedValue());
		}
		if(gamechoicearray.length >= 2){
			applet.add(s2);
			s2.setBounds(200, 300, 600, 50);
			s2.setLabel(gamechoicearray[1].getTitle()+ "        on         " + gamechoicearray[1].getSystem()+ "   valued at   $" + gamechoicearray[1].getUsedValue());
		}
		if(gamechoicearray.length >= 3){
			applet.add(s3);
			s3.setBounds(200, 350, 600, 50);
			s3.setLabel(gamechoicearray[2].getTitle()+ "        on         " + gamechoicearray[2].getSystem()+ "   valued at   $" + gamechoicearray[2].getUsedValue());
		}
		if(gamechoicearray.length >= 4){
			applet.add(s4);
			s4.setBounds(200, 400, 600, 50);
			s4.setLabel(gamechoicearray[3].getTitle()+ "        on         " + gamechoicearray[3].getSystem()+ "   valued at   $" + gamechoicearray[3].getUsedValue());
		}
		if(gamechoicearray.length == 5){
			applet.add(s5);
			s5.setBounds(200, 450, 600, 50);
			s5.setLabel(gamechoicearray[4].getTitle()+ "        on         " + gamechoicearray[4].getSystem()+ "   valued at   $" + gamechoicearray[4].getUsedValue());
		}
		applet.add(incorrect);
		incorrect.setBounds(400, 550, 100, 25);
		incorrect.setLabel("None");
		applet.add(cancel);
		cancel.setBounds(600, 550, 100, 25);
		
	}
	
	private void mergeGame(Game game){
		
		pagenum = 0;
		connector.addGame(game);
		updateUserList();
		b1.setEnabled(true);
		b2.setEnabled(true);
		b4.setEnabled(true);
		b5.setEnabled(true);
		b6.setEnabled(true);
		b3.setEnabled(true);
		b7.setEnabled(true);
		indialog = false;
		
	}

	private void selectGame(int index){

		System.out.println("Selected Game " + index);
		if(index + (pagenum * 20) < gamearray.length){
			gameselected = index + (pagenum * 20);				
		applet.add(b7);
		b7.setBounds(350, 15, 100, 25);
		b7.setEnabled(true);
		}else{
			gameselected = -1;
		}
	}

	private void deleteGame(int index){

		if(connector.deleteGame(index) == true){
			System.out.println("Game Deleted");
			pagenum = 0;
			updateUserList();
		}
		
	}
	
	private void updateUserList(){
		
		gamearray = connector.getUserList(userid);	
		
		if(gamearray.length > 20 && gamearray.length > (pagenum + 1) * 20){
		    np.setEnabled(true);
		}
		if(pagenum == 0){
			pp.setEnabled(false);
		}
		
		calculateValue();
		
	}
	
	private void calculateValue(){
		int index = 0;
		totalvalue = 0;
		if(gamearray[index] != null){			
			while(index < gamearray.length){
				if(gamearray[index] != null){
					totalvalue = totalvalue + gamearray[index].getUsedValue();
				}
				index++;
			}			
		}
	}

	private void userLogin(){

		/*
		 * This method will check user info with the server
		 * Currently it just accepts demo and demo
		 */

		if(connector.userLogin(userid, pass) == true){
			
			System.out.println(userid + " has logged in");
			userloginwrong = false;
			userisloggedin = true;
			username.setText("");
			password.setText("");
			applet.remove(username);
			applet.remove(password);
			applet.remove(submitUN);			
			b1.setEnabled(true);
			b2.setEnabled(true);
			b4.setEnabled(true);
			b5.setEnabled(true);
			b6.setEnabled(true);
			applet.remove(b0);
			applet.add(b3);
			b3.setBounds(490, 15, 100, 25);
			b3.setEnabled(true);
			indialog = false;
			updateUserList();
			
		}else{
			
			userloginwrong = true;
			
		}
		
		

	}

	public boolean action (Event e, Object args){

		/*
		 * This methods defines what happens when you click the buttons
		 */

		if (e.target == b0){
			System.out.println("User Login");
			applet.add(username);
			username.setBounds(410, 350, 200, 25);
			username.setEnabled(true);
			applet.add(password);
			password.setBounds(410, 380, 200, 25);
			password.setEnabled(true);
			applet.add(submitUN);
			submitUN.setBounds(630, 380, 100, 25);
			submitUN.setEnabled(true);
			indialog = true;

		}
		if (e.target == b1){
			System.out.println("Add Game");
			b3.setEnabled(false);
			addGame();
		}
		if (e.target == b2){
			System.out.println("Manual Update");
			updateUserList();
		}
		if (e.target == b3){
			System.out.println("User Logout");
			userisloggedin = false;
			b1.setEnabled(false);
			b2.setEnabled(false);
			b4.setEnabled(false);
			b5.setEnabled(false);
			b6.setEnabled(false);
			pp.setEnabled(false);
			np.setEnabled(false);
			applet.remove(b7);
			applet.remove(b3);
			applet.add(b0);
			b3.setBounds(490, 15, 100, 25);
			b3.setEnabled(true);
			pagenum = 0;
		}
		if (e.target == b4){
			System.out.println("Sort by Title");	        
		}
		if (e.target == b5){
			System.out.println("Sort by System");	        
		}
		if (e.target == b6){
			System.out.println("Sort by Used Value");	        
		}
		if (e.target == b7){
			System.out.println("Delete Game");
			deleteGame(gameselected);
		}
		if (e.target == submitGT){
			gameTitle = gametitle.getText();
			gametitle.setText("");
			selectConsole();
		}
		if (e.target == cancel){
			gametitle.setText("");
			
			b1.setEnabled(true);
			b2.setEnabled(true);
			b4.setEnabled(true);
			b5.setEnabled(true);
			b6.setEnabled(true);
			b3.setEnabled(true);
			b7.setEnabled(true);
			applet.remove(gametitle);
			applet.remove(submitGT);
			applet.remove(cancel);
			applet.remove(correct);
			applet.remove(incorrect);
			applet.remove(tryagain);
			hideConsoles();
			hideGameSelections();
			incorrectgame = false;
			indialog = false;
		}
		if (e.target == submitUN){
			System.out.println("Submit User Login Information");
			userid = username.getText();
			pass = password.getText();
			userLogin();			
		}
		if (e.target == correct){
			applet.remove(correct);
			applet.remove(incorrect);
			applet.remove(cancel);
			singlechoice = false;
			mergeGame(gametoadd);
		}
		if (e.target == incorrect){
			applet.remove(correct);
			applet.remove(incorrect);
			hideGameSelections();
			incorrectgame = true;
			singlechoice = false;
			multichoice = false;
			applet.add(tryagain);
			tryagain.setBounds(400, 500, 100, 25);
			cancel.setBounds(600, 500, 100, 25);
		}
		if (e.target == tryagain){
			applet.remove(tryagain);
			applet.remove(cancel);
			incorrectgame = false;
			addGame();
		}
		if (e.target == s1){
			applet.remove(incorrect);
			applet.remove(cancel);
			hideGameSelections();
			mergeGame(gamechoicearray[0]);
		}
		if (e.target == s2){
			applet.remove(incorrect);
			applet.remove(cancel);
			hideGameSelections();
			mergeGame(gamechoicearray[1]);
		}
		if (e.target == s3){
			applet.remove(incorrect);
			applet.remove(cancel);
			hideGameSelections();
			mergeGame(gamechoicearray[2]);
		}
		if (e.target == s4){
			applet.remove(incorrect);
			applet.remove(cancel);
			hideGameSelections();
			mergeGame(gamechoicearray[3]);
		}
		if (e.target == s5){
			applet.remove(incorrect);
			applet.remove(cancel);
			hideGameSelections();
			mergeGame(gamechoicearray[4]);
		}
		if (e.target == np){
			pagenum++;
			pp.setEnabled(true);
			if(gamearray.length <= (20 * (pagenum + 1))){
				np.setEnabled(false);
			}
			applet.remove(b7);
		}
		if (e.target == pp){
			pagenum--;
			np.setEnabled(true);
			if(pagenum == 0){
				pp.setEnabled(false);
			}
			applet.remove(b7);
		}
		if (e.target == xbox360){
			console = "360";
			hideConsoles();
			checkGame();
		}
		if (e.target == xbox360){
			console = "360";
			hideConsoles();
			checkGame();
		}
		if (e.target == xbox){
			console = "xbox";
			hideConsoles();
			checkGame();
		}
		if (e.target == ps3){
			console = "ps3";
			hideConsoles();
			checkGame();
		}
		if (e.target == ps2){
			console = "ps2";
			hideConsoles();
			checkGame();
		}
		if (e.target == ps1){
			console = "ps1";
			hideConsoles();
			checkGame();
		}
		if (e.target == psp){
			console = "psp";
			hideConsoles();
			checkGame();
		}
		if (e.target == pc){
			console = "pc";
			hideConsoles();
			checkGame();
		}
		if (e.target == wii){
			console = "wii";
			hideConsoles();
			checkGame();
		}
		if (e.target == gamecube){
			console = "gamecube";
			hideConsoles();
			checkGame();
		}
		if (e.target == n64){
			console = "n64";
			hideConsoles();
			checkGame();
		}
		if (e.target == snes){
			console = "snes";
			hideConsoles();
			checkGame();
		}
		if (e.target == nes){
			console = "nes";
			hideConsoles();
			checkGame();
		}
		if (e.target == ds){
			console = "ds";
			hideConsoles();
			checkGame();
		}
		if (e.target == genesis){
			console = "genesis";
			hideConsoles();
			checkGame();
		}
		if (e.target == dreamcast){
			console = "dreamcast";
			hideConsoles();
			checkGame();
		}
		if (e.target == other){
			console = "other";
			hideConsoles();
			checkGame();
		}
		return true;
	}

	private void loadImages(){

		System.out.println("Loading Images");
		
		/* Load images locally */
		
		//dialogoverlay = getImage(getDocumentBase(), "Images/dialogoverlay.png");
		//selectionbar = getImage(getDocumentBase(), "Images/selectionbar.png");
		
		/* Load images from the internet */
		
		try {
		dialogoverlay = getImage(new URL("http://web.cs.miami.edu/~mniznik/CSC531/fixGUI/Images/dialogoverlay.png"));
		selectionbar = getImage(new URL("http://web.cs.miami.edu/~mniznik/CSC531/fixGUI/Images/selectionbar.png"));
		} catch(MalformedURLException e) {e.printStackTrace();}
		
		/* Test draw the images just to be safe. */

		for(int i=0;i<3;i++) {
			bufferImage.flush();

			bufferGraphics.drawImage(dialogoverlay, 0, 0, null);
			bufferGraphics.drawImage(selectionbar, 0, 0, null);
	}

	}	

	public void mouseClicked(MouseEvent click) {

		int location = click.getY();
		int width = 30;
		int ylocation = 75;
		System.out.println("Mouse at " + location);

		if(indialog == false && userisloggedin == true){
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(0);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(1);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(2);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(3);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(4);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(5);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(6);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(7);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(8);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(9);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(10);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(11);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(12);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(13);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(14);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(15);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(16);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(17);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(18);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(19);
			}
			if(ylocation < location && location < (ylocation = ylocation + width)){
				selectGame(20);
			}
		}
		
	}

	public void mouseEntered(MouseEvent arg0) {


	}

	public void mouseExited(MouseEvent arg0) {


	}

	public void mousePressed(MouseEvent arg0) {


	}

	public void mouseReleased(MouseEvent arg0) {


	}

	/* niznik: Things to prevent flickering and deal w/ threading... */

	public void actionTracker() {
		repaint();
		try {
			Thread.sleep(sleeptime);
		} catch(InterruptedException ex) {
			System.out.println("Thread getStarted interrupted");
		}
	}

	/* Start both threads. */

	public void start() {
		init();
		getStarted = new Thread(this);
		getStarted.start();
		loadingThread = new Thread(this);
		loadingThread.start();
	}

	/* After the loading thread is done, join it. Have the game thread do what the actionTracker says as well. */

	public void run() {
		while(Thread.currentThread() == getStarted) {
			actionTracker();
		}
		if(Thread.currentThread() == loadingThread) {
			executePreLoad();
			try {
				loadingThread.join(1000);
				System.out.println("Thread loadingThread killed successfully!");
			} catch (InterruptedException e) {
				System.out.println("ERROR: Thread loadingThread could not be killed");
			}
		}
	}
	
	public void executePreLoad() {
		loadImages();		
	}


}
