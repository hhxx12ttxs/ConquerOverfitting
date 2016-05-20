/**
 * u4909228 JJY
 * Aug 28 2011 ~ 
 * 
 * uses perl script to connect with gnugo-gtp
 * 
 * perl twogtp-a --white './gnugo-3.8/interface/gnugo --mode gtp' --black './prac/test.jar' --verbose 2
 * 
 * [1] Wiki, Go, Recognising Legal Moves
 * 
 * 
 */

/**
 * With MChain and MBoard
 * 9, October, 2011
 */


import java.io.IOException;
import java.util.Random;
import java.util.Scanner;


public class Yu_c {
	
	static int history_size=0; //sko_i = 0; //iterator		
	static int history_max = 30;
	static MBoard [] sko_b = new MBoard[history_max]; //records history for superko check 
	static MBoard sb;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Random ran_gen = new Random();
		// TODO Auto-generated method stub
		//byte[] cmd = new byte[40]; //command from perl script
		char[] cmd = new char[40];
		int len; //input length
		int x,y; //x,y coordinates
//		int [][]b = new int [20][20]; //board
		int i,j; //for loops
		int nm; //number of moves done so far. used for opening
		int dC = 0; //dummy color
		
		int my_color = 0; //the color for this engine 
		
		int next = -1;//new Point(-1,-1); // next move
		
		boolean superko = true; // use superko rule ...
		boolean white_passed = false;
		
		boolean use_RAVE = true;
		boolean use_PureMC = false; //(non tree search )
		boolean early_termination = true;
		
		double komi = 5.5;
		
		int Board_size = 9;
		int max_iter = 3000;		
	
		MBoard b = new MBoard(Board_size);
		MBoard testB = null;
		nm = 0;
		

		
		
		UCT_c gb = new UCT_c(b, -1, max_iter, use_PureMC, use_RAVE, komi);
		
		
		
		Scanner sc = new Scanner(System.in);		
			
		
		
		while(true) {
			//reads command from perl			
			System.out.println("");
			System.out.println("");
			System.in.skip(0);
			String input;// = sc.next();			
			 try{				    
	                input = sc.nextLine();
	                
            } catch( Exception e ){
                System.out.println( ":: EXCEPTION: " + e.toString() );
         
	                
                System.out.println( ":: Looks like stdin is broke" );
	                    
                break;
	                
            }			
            sc.reset();
	            
			len = input.length();					
			cmd = input.toCharArray();
			
			if ( len > 1 ) {

				if ( cmd[0] == 'b' ){				//cmd input = boardsize #
					if ( len == 11 ) //1 digit board size
						Board_size = cmd[10]-48;
					
					else if ( len == 12 ) //2 digit board size
						Board_size = (cmd[10]-48)*10 + cmd[11]-48;
					
					b = new MBoard(Board_size);
				//	testB = null;
					nm = 0;
					
					white_passed = false;
					
					System.out.println("");
					System.out.println("= ");
					System.out.println("");
				}
				
				else if (cmd[0] =='k' && cmd[1] =='o'){	//cmd input = komi #
					//komi value input
					if ( len != 8 || cmd[6] != '.' || cmd[5] < 48 || cmd[5] > 57 || cmd[7] < 48 || cmd[7] > 57){
						komi = 5.5;
						System.out.println("Wrong komi value, should be in form \n komi = #.#");
					}
					else {
						komi = cmd[5]-48;					
						komi += (double)(cmd[7]-48)/10;
					}		
					gb = new UCT_c(b, -1, max_iter, use_PureMC, use_RAVE, komi);
					System.out.println("");		
					System.out.println("= ");
					System.out.println("");										
				}
				
				else if (cmd[0] =='f' && cmd[1] =='i'){	//cmd input = final_score
					System.out.println("");
					System.out.println("= "+gb.final_score(b, my_color));
					System.out.println("");
				}
				
			    else if (cmd[0] =='p' && cmd[1] =='r'){	//cmd input = protocol version
					//asking protocol version
					System.out.println("");
					System.out.println("= 2");
					System.out.println("");
				}
				
			    else if (cmd[0] =='s' && cmd[1] =='h'){	//cmd input = show board
					//asking protocol version
					
					System.out.println("= \n");
					System.out.print(b.GnuGo_style_Board_String());
				}
				
			    else if (cmd[0] =='c' && cmd[1] =='l'){	//cmd input = clear
					//clears the board
					
			    	b = new MBoard(Board_size);
			    	sko_b = new MBoard[history_max]; 
			    	history_size = 0;
			    	
					System.out.println("= \n");
					//System.out.print(b.GnuGo_style_Board_String());
				}
				
			    else if (cmd[0] =='c' && cmd[1] =='h'){	//prints chain-id s == for debugging
					
					System.out.println("= \n");
					System.out.print(b.toString());
				}
				
				else if (cmd[0] == 'p' && cmd[1] == 'l'){	//cmd input = play white XX
					//'play "COLOR" "Position" part, tells us what the player has played' 
					// we store the player's ( black or white ) play in to the board
					
					//if 'play white a1' , white's play
					if (cmd[5] == 'w') {
							dC = 1;
							white_passed = false;
					}
					else //else, it must be 'play black a1' , black's play
						dC = -1;
					
					System.out.println("");
					if (len == 13) {
						//1 digit y axis
						x = cmd[11]-97;
						y = cmd[12]-49;
						if ( x > 8 ) 
							x--; //GHJK, no I						
											
						b = process(b,x+y*Board_size, dC, false, superko);
						/*
						if ( b.processMove(x+y*Board_size, dC, false) ) 
						//System.out.println( (x)+" "+(y));
							System.out.println("= ");
						else 
							System.out.println("= Illegal");
							*/
					}					
					else if ( len == 14 ){
						//2 digit y axis
						x = cmd[11]-97;
						y = (cmd[12]-48)*10+cmd[13]-49;
						
						if ( x > 8 ) 
							x--; //GHJK, no I
						
						b = process(b,x+y*Board_size, dC, false, superko);
						/*
						if (b.processMove(x+y*Board_size, dC, false))
							System.out.println("= ");
						else 
							System.out.println("= Illegal");
							*/
					}
					else {
						//if its not 13 nor 14, it should be PASS from white
						System.out.println("= ");
						if ( dC == 1 )
							white_passed = true;
					}
					
					nm++;
				}							
				
				else if (cmd[0] == 'g') {	//cmd input = genmove
					my_color = -1; //can be changed later to include case for this engine being white 
					//Generates black's move
					System.out.println("");					
					
					gb.reset();
					
					Long time_used = System.nanoTime();
					next = gb.Next(b, my_color, max_iter);
					time_used -= System.nanoTime();
										
					nm++;
					
					//System.out.println(next.x + " "+next.y);
					
					
					
					//early termination					
					if (early_termination && white_passed && (gb.final_score(b, -1) > 0 ) ) {
						System.out.println("= PASS");							
					}
					else if ( next == -1 )
						//if the simulation wants to pass ( this includes no legal moves )
						System.out.println("= PASS");
					else {						
						if(superko){
							int chc = gb.sTree.child_count;
							//speed is not a concern in this part. 
							double [] vslist = new double[chc];
							double dummy;
							int maxi;
							for (int si =0 ; si < chc; si++){
								vslist[si] = gb.sNode[gb.sTree.child[si]].visit;
							}
							
							for (int si =0 ; si < chc-1; si++){
								maxi = si;
								for (int sj = si ; sj < chc; sj++){
									if (vslist[maxi] < vslist[sj]){	
										maxi = sj;
									}
								}
								dummy = vslist[si];
								vslist[si] = vslist[maxi];
								vslist[maxi] = dummy;								
							}					
							int si;
							for (si =0 ; si < chc; si++){								
								for (int sj =0 ; sj < chc; sj++){
									if ( vslist[si] == gb.sNode[gb.sTree.child[sj]].visit ) {
										next = gb.sNode[gb.sTree.child[sj]].from_parent_to_this_node;
										break;
									}
								}
																	
								testB = b.clone();
								testB.processMove(next, my_color, false);
								if (no_superko(testB)){
									break;
								}
							}	
							
							if ( si == chc) //if all the moves are superko
								next = -1;
						}			
						
						if ( next == -1 )
							System.out.println("= PASS");
						else if ( b.processMove(next, my_color, false)) {
							i = next%Board_size;
							j = next/Board_size;
							if ( i > 7 )
								i++; //G->H->J->K, no I
							i += 97;
							j+=1;
							System.out.print("= ");
							System.out.print((char)i);
							System.out.println(j);
							//break;
						}
					}							
				}
				else if (cmd[0] == 'r' ){ //vs. random player
					
					boolean pass_b = false;
					
					boolean pass_w = false;
					int turn=-1;
					int count = 0;
					
					UCT_c a = new UCT_c(b, 1, 40000,true,false,komi);
					UCT_c aa = new UCT_c(b, -1, 1000,false,false,komi);
					
					while(!pass_b || !pass_w){
						System.out.println("Ter val = "+a.Teritory_Evaluation(b));
						if ( turn == 1 ){
							a.reset();		
							Long time_used = System.nanoTime();
							
							next = a.Next(b, 1, 40000);
							
							time_used -= System.nanoTime();
							System.out.println(next+" at "+time_used/1000000000.0);
							if (next == -1) {
								System.out.print("black PASS");
								pass_b=true;
							}
							else {
								b.processMove(next, turn, false);
								pass_b = false;
							}
							//a = null;
							
						}
						/*for ( int ii = 0; ii < Board_size*Board_size; ii++){
							if ( b.processMove(ii, turn ))
								break;
						}*/
						else if ( turn == -1 ){
							//UCT_c a = new UCT_c(b, 1, 1000);
							aa.reset();
							Long time_used = System.nanoTime();
							aa.genLegalChild(b, -1);
							if ( aa.LegalMoves_size != 0 )
								next = aa.LegalMoves[ran_gen.nextInt(aa.LegalMoves_size)];
							else 
								next = -1;
							
							//next = aa.Next(b, -1, 1000);							
							
							time_used -= System.nanoTime();
							System.out.println(next+" at "+time_used/1000000000.0);
							if (next == -1) {
								System.out.print("white PASS");
								pass_w = true;
							}
							else {
								b.processMove(next, turn, false);
								pass_w = false;
							}
							//a= null;
						}
						turn = turn*-1;
						System.out.println(count++ +"\n"+b.GnuGo_style_Board_String());
					}
					System.out.println(count++ +"\n"+b.toString());
				}
				
				else if (cmd[0] == 'n' ){ //vs. self
					
					boolean pass_b = false;					
					boolean pass_w = false;
					int turn=-1;
					int count = 0;
					
					int b_iter = 3000;
					int w_iter = 3000;
					
					UCT_c black = new UCT_c(b, -1, b_iter,false,false,komi);
					UCT_c white = new UCT_c(b, 1, w_iter,false,true,komi);
					
					while(!pass_b || !pass_w){
						//System.out.println("Ter val = "+black.Teritory_Evaluation(b));

						if ( turn == -1 ){
							black.reset();		
																					
							Long time_used = System.nanoTime();	
						/*	if ( pass_w && ( black.final_score(b, turn) > 0 ) )
								next = -1;
							else 
							*/	next = black.Next(b, turn, b_iter);		
							
							time_used -= System.nanoTime();
							
							System.out.println(next+" at "+time_used/1000000000.0);
							if (next == -1) {
								System.out.println("black PASS");
								pass_b=true;
							}
							else {
								b.processMove(next, turn, false);
								pass_b = false;
							}
							//a = null;
							
						}
						/*for ( int ii = 0; ii < Board_size*Board_size; ii++){
							if ( b.processMove(ii, turn ))
								break;
						}*/
						else if ( turn == 1 ){
							//UCT_c a = new UCT_c(b, 1, 1000);
							white.reset();
							Long time_used = System.nanoTime();
							
						/*	if ( pass_b && ( white.final_score(b, turn) > 0 ) )
								next = -1;
							else 
							*/	next = white.Next(b, turn, w_iter);	
													
							
							time_used -= System.nanoTime();
							System.out.println(next+" at "+time_used/1000000000.0);
							if (next == -1) {
								System.out.println("white PASS");
								pass_w = true;
							}
							else {
								b.processMove(next, turn, false);
								pass_w = false;
							}
							//a= null;
						}
						turn = turn*-1;
						System.out.println(count++ +"\n"+b.GnuGo_style_Board_String());
					}
					System.out.println("Ter val = "+black.final_score(b, -1));
					System.out.println("captured Blacks = "+b.capB);
					System.out.println("captured Whites = "+b.capW);
					System.out.println(count++ +"\n"+b.toString());
				}
				
				else { // other cmd inputs
					System.out.println("");
					System.out.println("= ");
					System.out.println("");
				}
			}
			else { // other cmd inputs
				System.out.println("");
				System.out.println("= ");
				System.out.println("");
			}
			
		}

	}
	
	//Following is from [1]
	/*
	private static Board processMove(Board board, Board Tb, Point move, Color color) {
		Tb = board.processMove_Board(move, color);
		if (Tb == null) {
			//System.err.println("Illegal move!");
			return board;
		} else {
			//System.out.println(Tb);
			return Tb;
		}		
	}
	*/
	//gives you random integer within range
	/*public static int randint(int range) {
	    double d = java.lang.Math.floor(java.lang.Math.random() * range);
	    return (int)java.lang.Math.round(d);
	}
	*/
	public static MBoard process(MBoard b, int move, int turn, boolean heuristic, 
			boolean superko){

		if ( superko) {
			sb = b.clone();
			if (sb.processMove(move, turn, heuristic)) {
				if (no_superko(sb)){	
					b = sb.clone();					
					sko_b[history_size%history_max] = b.clone();
					history_size++;
					System.out.println("= ");
				}
				else {
					System.out.println("= SuperKo");
				}
			}
			else {
				System.out.println("= Illegal");
			}
		}
		else {
			if (b.processMove(move, turn, heuristic)) {
				
				sko_b[history_size%history_max] = b.clone();
				history_size++;
				System.out.println("= ");
			}
			else {
				System.out.println("= Illegal");
			}
		}
		return b;
	}
	
	public static boolean no_superko(MBoard sb){
		
		for (int sj = 0; sj < history_max; sj++ ){
			//System.out.println("sj = "+sj);
			if ( sko_b[sj] != null) {
				//System.out.println(sko_b[sj].GnuGo_style_Board_String());
				boolean same = true;
				
				for ( int sk =0; sk < sb.sq_boardSize; sk++ ){
					
					if( sko_b[sj].getColor(sk) != sb.getColor(sk)){
						same = false;
						break;
					}
				}
				if (same)
					return false; //board have been repeated
			}

		}
		
		return true;
	}
}

