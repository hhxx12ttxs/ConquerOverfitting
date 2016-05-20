
import java.util.Random; //*;

/**
 * This version is with MChain and MBoard
 * 
 * from 9, October, 2011 ~
 * 
 * @author u4909228, JJY
 *
 * 
 * 
 * RAVE UCT
 */

class UCTnode_c{ //search node for the UCT 
	MBoard b=null;
	int from_parent_to_this_node;//used when the node is not expanded, this is just the last move 
	
	int turn; //tells us which palyer's turn it is	
	
	double visit; // number of visits done to this node	
	double xbar;
	double ucb1 = 0.0; //value used by UCB1 algorithm
	double square_sum_xbar;
	
	//for all moves as first heuristic
	double amaf_visit;
	double amaf_xbar_sum;
	
	UCTnode_c parent = null;

	int [] child;
	int child_count;
	
	public UCTnode_c(MBoard cb, int turn){
		
		this.child = new int[cb.sq_boardSize+1];//+1 for PASS
		
		this.reset_node(turn, 0, null, 0);
		this.turn = turn;
		
		if ( turn != 10  ) { // for initialization purpose...
			this.b = new MBoard(cb.boardSize);
			this.b.copy(cb);					
		}
	}
	
	public void reset_node(int input_turn, int fptt, UCTnode_c parent, double tiebreaker){
		this.parent =parent;

		this.square_sum_xbar = 0.0;
		this.from_parent_to_this_node = fptt;
		//b = null;		
		this.xbar = tiebreaker/1000.0; // no need to initialise this value, as it is going to be used only after the node is expanded
		this.ucb1 = (tiebreaker+1.0)*1000.0; // optimistic initial value
		
		
		this.visit = tiebreaker*0.00001;//( can be just 1 as well )
		if (this.visit < 0)
			this.visit = this.visit*(-1);

		//visit+=1.0;
		
		this.child_count = 0;
		
		this.turn = input_turn;
		
		this.amaf_visit = 0.0;
		this.amaf_xbar_sum = 0.0;		
	}
}

public class UCT_c {
	
	int [] empty_positions;
	int empty_size; // number of empty positions on the board
	
	int MC_iter_i, MC_iter_k, MC_iter_turn;
	int Over_child;
	int gLC_testmove;
	int gL_iterator;
	
	// used in genLegal
	int Li,Lj,Lk, Lh, Lko, gLi; 
	MBoard LTb, Mmb;
	
	int UC_i; //updatechild iterator
	//UCTnode_c UC_mm; // dummy
	
	int next_i, next_j;
	
	int testmove ;//= new Point(-1,-1);
	
	Random rgen = new Random();
	
	int board_size; // size of the board
	int sq_board_size; //squared board size
	double ratio = 0.2;//0.2;//exploration / exploitation tradeoff 
	int i;
	int n; //number of iterations done	
	UCTnode_c sTree; //root of search tree, for expanded nodes
	UCTnode_c[] sNode;
	int sNode_count;
	int sNode_count_max;
	int sNode_count_next; 
	
    // record of moves for update purpose
	int [] MadeMoves;
	int MadeMoves_size;
	UCTnode_c mm;
	
	int[] LegalMoves; //legal moves from current board
	int LegalMoves_size;
	
	boolean react_to_atari;
	
	//for updating
	double V_j;
	double log_n;
	
	//for update_tree
	double Leaf_xbar_white_turn;	
	double Leaf_xbar_black_turn;
	double Leaf_xbar;
	
	//for teritory evaluation 
	int nb=0,nw=0;
	int Ti, Tj, Tk, Tl;
	boolean [] Tempty;
	//int Tc;
	//int []Teb;
	int []Tva;
	int Tvn;
	MBoard DA;
	int []ETEChaincounter;
	
	
	//for simulation moves
	int []canmove;
	int canmove_size;
	int gSi; //iterator
	int gSdummy;
	int Ls;
	
	
	//================================
	//Variables for all moves as first heuristic
	//================================
	//true : UCT-RAVE
	//false : UCT
	boolean amaf; 
	
	//moves made in simulation
	//0  : not used in simulation
	//-1 : used by black in simulation
	//1  : used by white in simulation
	int []sim;
	
	int UT_iterator;
	double amaf_k; // number of simulations where RAVE and normal UCT has same weight
	//should be empirical ... let's assume 1/3 at here.. as the paper had k = 1000
	//when the number of simulation was 3000
	
	double wt_param; // the beta term in paper ( 2011 Silver & Gelly ch4.3)
	double nwt_param; //1 - wt_param
	//weighting parameter
	
	double komi;
	int komi_int;
	
	boolean pureMC;
	
	int numnode;
	
	public UCT_c(MBoard b, int turn, int max_iter, boolean pureMC_in, boolean use_RAVE, double komi_in){
		pureMC = pureMC_in;
		komi = komi_in;
		komi_int = ((int)(komi_in*10.0))/10;
		amaf = use_RAVE;
		sim = new int [b.sq_boardSize+1];
		
		LegalMoves = new int [b.sq_boardSize+1];//+1 for empty move 
		LegalMoves_size = 0;
		
		sNode_count_max = (max_iter / 1) * (b.sq_boardSize);
		sNode_count_next=1;
		sNode_count = 0;
		
		sTree = new UCTnode_c(b,turn);			
		sNode = new UCTnode_c[sNode_count_max];//(b,turn); 
		//250000 ~= 30000 / 10 * 81 = iter / 10per expanding * max legal child 
		for (  i = 0; i < sNode_count_max; i++)
			sNode[i] =new UCTnode_c(b,10);//(b,turn); 
		
		//currentBoard = b;
		n=0;
		board_size = b.boardSize;
		sq_board_size = board_size*board_size;
		
		LTb = new MBoard(board_size); //b.clone();
		
		Tempty = new boolean [sq_board_size];
		
		//Teb = new int [sq_board_size];
		Tva = new int [sq_board_size];
		
		canmove = new int [sq_board_size];
		
		MadeMoves = new int[sq_board_size];
		empty_positions = new int[sq_board_size];
		ETEChaincounter = new int[sq_board_size];
		
		//for stats. 
		numnode = 0;
	}
	
	public void reset(){		
		LegalMoves_size = 0;	
		
		sNode_count_next=1;
		sNode_count = 0;
		
		n=0;
		
		numnode = 0;
	}
	
	//gives the searched, final next move
	public int Next(MBoard b,int cturn, int Max_iter){ 
		
		sNode_count_next=1;
		ratio = 0.2;//1.0;
		//amaf_k = 10000.0;
		if ( amaf ) {
			ratio = 0.12;//Math.sqrt(2.0);//0.0;//14;//0.14;//0.01;//0.03;
			amaf_k = (double) Max_iter / 3.0;
		}
				
		int num_ex_node = 0;
		//int num_node = 0;
		
		int nextmove = -1;// = new Point(-1,-1);
		//UCTnode_c sNode; //search node, current position in the tree
		
		UCTnode_c c; // search node, dummy
		//int turn=cturn;
		int depth = 0; //depth of the search tree
		int max_depth = 0;
		
		n= 0;
		
		//MadeMoves = new ArrayList<UCTnode_c>();; //records which nodes in the tree part have been used
		MadeMoves_size = 0;
		

		for ( i = 0; i < board_size*board_size; i++) {			
			empty_positions[i] = i;//new Point(i%board_size,i/board_size);			
		}
				
		if ( pureMC ){
			//only Monte Carlo samples on the root node
			
			//the root node
			sTree = new UCTnode_c(b.clone(),cturn);
			
			//expand the root node
						
			sNode[0] = sTree;
			sNode[0].visit ++;		
			sNode_count = 0;
					
						
			if ( genLegalChild(sTree.b, cturn) == false ) {					
				return -1;					
			}
			else {
				//there are legal moves, we create child of our root	
				
				
				for ( Over_child = 0; Over_child < LegalMoves_size; Over_child++  ){
					//num_node++;
					sNode[sNode_count].child[sNode[sNode_count].child_count++] = sNode_count_next;
					sNode[sNode_count_next].reset_node(
							-1*sNode[sNode_count].turn, //child is opponent player
							LegalMoves[Over_child],
							sNode[sNode_count],rgen.nextGaussian());	
										
					sNode[sNode_count_next].b = sNode[sNode_count_next].parent.b.clone();						
					sNode[sNode_count_next].b.processMove(sNode[sNode_count_next].from_parent_to_this_node, sNode[sNode_count_next].parent.turn, false);
					
					sNode_count_next++;	
				}		
			}
						
			while ( n < Max_iter ){	//runs until maximum iteration has reached, can be changed into time limited search
							
				n++;
				next_j = rgen.nextInt(sNode_count_next-1)+1;				
				sNode[next_j].visit++;
				
				//MadeMoves.clear();
				//MadeMoves.add(sNode[next_j]);
				MadeMoves[0] = next_j;
				MadeMoves_size = 1;
				
				MC(sNode[next_j],false); //we do the MC		
			}		
		}
		else {
			//-----------------------------
			//
			//		UCT method
			//
			//-----------------------------
			//   if ratio = 0.0, then MC tree search (greedy) 
			
			n = 0;
			
			//Sets the root node
			sTree = new UCTnode_c(b.clone(),cturn);
			sTree.visit = 0.0; 
			sNode[0] = sTree;

			//we expand root right away			
			expandcurrentUCTnode();
			
			while ( n < Max_iter ){	//runs until maximum iteration has reached, can be changed into time limited search		
				//System.out.println(n);
				n++;
				depth = 0;
				//from 2nd iteration, we need to search down the tree
				//This is to record the selected route in the tree part.	
				
				MadeMoves_size = 0;						
				sNode_count = 0;		
				
				//======================
				//Tree Search Part
				//======================
				while ( sNode[sNode_count].child_count!=0 ){
					depth++;						
					sNode[sNode_count].visit += 1.0;
					MadeMoves[MadeMoves_size++] = sNode_count;
					UpdateChild(sNode[sNode_count]);
					
					c = sNode[sNode[sNode_count].child[0]];
					next_j = 0;
					//exp_epl = n%10;//rgen.nextInt(2);
					
					//picks next child according to ucb1 value
					for ( next_i = 1; next_i < sNode[sNode_count].child_count; next_i++) {//
						//UCTnode_c a : sNode.child ){
						
						//UC_mm = sNode[sNode[sNode_count].child[next_i]];
						//if (c.ucb1 < UC_mm.ucb1){

						if (c.ucb1 < sNode[sNode[sNode_count].child[next_i]].ucb1){										
							//	System.out.println("n= "+n+" next_i = "+next_i+" "+c.ucb1+" "+UC_mm.ucb1);								
							//c = UC_mm;
							c = sNode[sNode[sNode_count].child[next_i]];
							next_j = next_i;
						}							
					}
					//	System.out.println("n : "+n+"depth : "+depth+" next_j = "+next_j+" : "+sNode[sNode[sNode_count].child[next_j]].ucb1);
					sNode_count = sNode[sNode_count].child[next_j];
					
					//if ( 2 > sNode[sNode_count].visit ) {
					/*if ( sNode[sNode_count].parent == sNode[0] ) {						
						System.out.println(sNode[sNode_count].from_parent_to_this_node);
						System.out.println(sNode[sNode_count].ucb1);
						System.out.println(sNode[sNode_count].visit);
					}*/
									
				}
				
				if ( depth > max_depth ) {
					max_depth = depth;
				//	System.out.print(max_depth+" ");
				}
				//System.out.println("");
				//MadeMoves.add(sNode[sNode_count]);
				sNode[sNode_count].visit += 1.0;	
				MadeMoves[MadeMoves_size++] = sNode_count;
				//System.out.println(n+"\nturn:"+sNode.turn.toString()+" visit:"+sNode.visit+"\n"+sNode.b.toString());
				
				
				//======================
				//Expand and MC part
				//======================							
				//we have arrived at the leaf of the search tree
				//we expand this node
				
				//System.out.println("iter="+n+" d="+depth);
				
				//If last 2 moves were pass, then the game has ended... we udate here
				if (	MadeMoves_size > 1 &&
						sNode[MadeMoves[MadeMoves_size-1]].from_parent_to_this_node == -1 &&
						sNode[MadeMoves[MadeMoves_size-2]].from_parent_to_this_node == -1)
				{
					UpdateTree(sNode[MadeMoves[MadeMoves_size-2]].b, MadeMoves, MadeMoves_size);	
				}	
				else if ( sNode[sNode_count].visit < 10.0 ) { 
					//following Goanna's way, doesn't expand until visit reaches 10'
					//root is always expanded
					
					MC(sNode[sNode_count], true);						
				}
				else {
					//our 10th visit to this node, we do expand it now 
					
					if ( sNode[sNode_count].b == null ) {
						//System.out.println(sNode.visit);
						sNode[sNode_count].b = sNode[sNode_count].parent.b.clone();						
						sNode[sNode_count].b.processMove(sNode[sNode_count].from_parent_to_this_node, sNode[sNode_count].parent.turn, false);
						num_ex_node++;
					}
					else {//if exists, then copy 						
						sNode[sNode_count].b.copy(sNode[sNode_count].parent.b);						
						sNode[sNode_count].b.processMove(sNode[sNode_count].from_parent_to_this_node, sNode[sNode_count].parent.turn, false);
						num_ex_node++;
					}
					// we create child of this node	
					
														
					expandcurrentUCTnode();
					/*
					genLegalChild(sNode[sNode_count].b, sNode[sNode_count].turn);			
					for ( Over_child = 0; Over_child < LegalMoves_size; Over_child++  ){
						//num_node++;
						sNode[sNode_count].child[sNode[sNode_count].child_count++] = sNode_count_next;
						sNode[sNode_count_next].reset_node(
								-1*sNode[sNode_count].turn, //child is opponent player,
								LegalMoves[Over_child],
								sNode[sNode_count], //this node is the parent of its child
								rgen.nextDouble());							
						sNode_count_next++;			
					}*/
					MC(sNode[sNode_count],false); // MC						
				}				
			}			
		}
		
		//System.out.println("max_depth = "+max_depth);
		
		//if there is no legal move left for this player,
		//error move prints
		if (sTree.child_count == 0) 
			return nextmove; //this is -1		
		
		c = sNode[sTree.child[0]];//.get(rgen.nextInt(sTree.child.size()));
		
		for ( next_i = 0; next_i < sTree.child_count; next_i++ ){			
			//System.out.println("fptt"+a.from_parent_to_this_node+"\nxbar = "+a.xbar+"\nucb1 = "+a.ucb1+"\nvisit = "+a.visit+"\n");
			if ( c.visit < sNode[sTree.child[next_i]].visit ) { //can be ucb1 or xbar				
				c =  sNode[sTree.child[next_i]];
				//System.out.println(c.b);
			}			
		}				
		
		//System.out.println(sTree.b.GnuGo_style_Board_String());
		//System.out.println(c.b.GnuGo_style_Board_String());
		if ( c.xbar < 0.001 ) //if the chance is too low, than pass
			return -1;
		else	
			return c.from_parent_to_this_node;//nextmove
	}
	
	public void expandcurrentUCTnode(){
		genLegalChild(sNode[sNode_count].b, sNode[sNode_count].turn);								
		for ( Over_child = 0; Over_child < LegalMoves_size; Over_child++  ){
			numnode++;
			sNode[sNode_count].child[sNode[sNode_count].child_count++] = sNode_count_next;
			sNode[sNode_count_next].reset_node(
					-1*sNode[sNode_count].turn, //child is opponent player,
					LegalMoves[Over_child],
					sNode[sNode_count], //this node is the parent of its child
					rgen.nextDouble());					
			sNode_count_next++;							
		}
	}
	
	public boolean genLegalChild(MBoard gb, int turn){		
		//int  testmove;// = new Point(-1,-1);		
		//int i,j; 		
		LegalMoves_size = 0;
		for ( gLC_testmove =0; gLC_testmove < sq_board_size; gLC_testmove++) {				
			if (gb.is_Legal(gLC_testmove, turn)){
				LegalMoves[LegalMoves_size++] = (gLC_testmove);				
			}
		}
		
		//add PASS move
		LegalMoves[LegalMoves_size++] = -1;
		
		if ( LegalMoves_size == 1  )
			return false;
		else
			return true;
	}
	
	
	//Generates simulation moves
	public boolean genSimMove(int turn){
		
		if ( empty_size == 0 )
			return false;
		
		Lk = 5;
		react_to_atari = false;
//Lh = -1;

		canmove_size = 0;
		
		//this testmove is last move
		if ( testmove != -1 && LTb.getColor(testmove) != turn ) {
			//react to opponent's self-atari
			Lh = LTb.in_Atari(testmove);
			if (Lh != -1 )
				react_to_atari = true;
	
			if ( Lh == -1 ) {
				//SimHeuristic 2
				//if some of our chain is in threat
				canmove_size = 0;
				for ( gL_iterator = 0; gL_iterator < sq_board_size; gL_iterator++){					
					if ( (!LTb.chains[gL_iterator].isempty()) && LTb.chains[gL_iterator].inAtari() ){
						//System.out.println("inatari : "+LTb.chains[gL_iterator].member[0]);
						if (LTb.getColor(LTb.chains[gL_iterator].member[0]) == turn ) {
							//our stone is in atari,
							//------------
							//Attempt 1
							//we see whether this can be saved by capturing opponent

							gSi = LTb.chains[gL_iterator].chain_size-1;
							//System.out.println( LTb.chains[gL_iterator].member[0] );
							//System.out.println( "turn = " +turn );
							for ( ; gSi >= 0; gSi-- ){ //careful...
								gSdummy = LTb.chains[gL_iterator].member[gSi];
								Ls = LTb.neighborchain_inAtari(gSdummy, 0, turn);
								if ( Ls != -1 ) {
								//	System.out.println( "chain_id : " +gL_iterator+ " point : "+gSdummy );
								//	System.out.println(" "+Ls+" 00");									
									canmove[canmove_size++] = Ls;									
								}																		
								Ls = LTb.neighborchain_inAtari(gSdummy, 1, turn);
								if ( Ls != -1 ) {
								//	System.out.println( "chain_id : " +gL_iterator+ "point : "+gSdummy );
								//	System.out.println(" "+Ls+" 01");									
									canmove[canmove_size++] = Ls;									
								}
								Ls = LTb.neighborchain_inAtari(gSdummy, 2,turn);
								if ( Ls != -1 ) {
								//	System.out.println( "chain_id : " +gL_iterator+ "point : "+gSdummy );
								//	System.out.println(" "+Ls+" 02");									
									canmove[canmove_size++] = Ls;									
								}
								Ls = LTb.neighborchain_inAtari(gSdummy, 3,turn);
								if ( Ls != -1 ) {
								//	System.out.println( "chain_id : " +gL_iterator+ "point : "+gSdummy );
								//	System.out.println(" "+Ls+" 03");								
									canmove[canmove_size++] = Ls;									
								}
							}							
						}
					}
				}
				if (canmove_size != 0 ) {
					react_to_atari = true;
					Lh = canmove[rgen.nextInt(canmove_size)];
				}
				/*
				if (canmove_size > 2 ) {
					System.out.println("--------------------");
					System.out.println("turn = "+turn);
					System.out.println(canmove_size);
					for (Ls = 0; Ls < canmove_size; Ls++)
						System.out.println(" "+canmove[Ls]);
					System.out.println(LTb.GnuGo_style_Board_String());
					System.out.println(LTb.toString());
					System.out.println("--------------------");
				}*/
			}
			
			
			if ( Lh == -1 ) {	
				canmove_size = 0;
				//SimHeuristic 1
				//move that captures some stones
				
				for ( gL_iterator = 0; gL_iterator < sq_board_size; gL_iterator++){					
					if (!LTb.chains[gL_iterator].isempty() && LTb.chains[gL_iterator].inAtari() ){
						//System.out.println("inatari : "+LTb.chains[gL_iterator].member[0]);
						if (LTb.getColor(LTb.chains[gL_iterator].member[0]) == turn*-1 ) {
							Lh = LTb.in_Atari(LTb.chains[gL_iterator].member[0]);
							//System.out.println("detected : "+Lh);
							if (LTb.is_Legal(Lh, turn)){								
								//System.out.println(LTb.GnuGo_style_Board_String());
								//System.out.println("turn : "+turn);
								//System.out.println("pos : "+LTb.chains[gL_iterator].member[0]);
								gSi = LTb.chains[gL_iterator].chain_size;
								//weights bigger chains more. .. 
								for ( ; gSi > 0; gSi-- ){
									canmove[canmove_size++] = Lh;									
								}
								//canmove[canmove_size++] = -1;
								//canmove[canmove_size++] = Lh;
								//break;								
							}								
						}
					}
				}
				
				if (canmove_size != 0 ) {
					react_to_atari = true;
					Lh = canmove[rgen.nextInt(canmove_size)];
				}
			}	
			
		}
		else
			Lh = -1;
		
		while(true) {			
			Lk--;
			if ( Lk == 0 ) { 
				// then we look for all empty positions 
				// and pick one of the legal empty positions, randomly
				
				if (genLegalChild(LTb, turn) != false){
					//last one is empty, so we remove it
					LegalMoves_size--;
					testmove = LegalMoves[ rgen.nextInt(LegalMoves_size)];
					
					//System.out.println(LegalMoves_size + " " + testmove+ " "+LegalMoves[LegalMoves_size-1]);
					
					for ( Li = 0; Li < empty_size; Li++){
						if ( testmove == empty_positions[Li]) {
							Lh = Li;
							break;
						}
					}
					
					if (Li == empty_size){
						for ( Li = 0; Li < empty_size; Li++){
							System.out.print(empty_positions[Li]+" ");
						}
						System.out.println("error in Lk==0 "+testmove);
						System.exit(-1);
					}
				}
			}			
			else if (react_to_atari){
				if ( Lh != -1 ) {
					testmove = Lh;
					for ( Li = 0; Li < empty_size; Li++){
						if ( testmove == empty_positions[Li]) {
							Lh = Li;
							break;
						}
					}	
				}
				else {
					Lh = rgen.nextInt(empty_size);			
					testmove = (empty_positions[Lh]);					
				}
				react_to_atari = false;
			}
			else {				
				//if any of the chain is in atari, it is carried out immediately				
				testmove = -1;
				//----------------------------
				//need to re-add the already captured positions to use following heuristic
				//---------------------------
				

				
				Lh = -1;
				if ( testmove == -1 ) {
					Lh = rgen.nextInt(empty_size);			
					testmove = (empty_positions[Lh]);
				}
				else {
					for ( Li = 0; Li < empty_size; Li++){
						if ( testmove == empty_positions[Li]) {
							Lh = Li;
							break;
						}
					}					
				}
				
				if ( Lh == -1 ) {
					System.out.println("error in genL");
					
					System.out.println(LTb.GnuGo_style_Board_String()+"\n"+testmove+" "+LTb.getColor(testmove));
					System.out.println(turn);
					System.out.println(empty_size);
					for ( Li = 0; Li < empty_size; Li++){
						System.out.print(empty_positions[Li]+" ");
					}
					System.out.println();
					for (; Li < LTb.sq_boardSize; Li++){
						System.out.print(empty_positions[Li]+" ");
					}
					System.exit(-1);
				}
								
			//System.out.println(testmove+" "+Lh+"/"+empty_size);
			}
			
			//System.out.println("testmove : "+testmove+"turn : "+turn);
			//System.out.println(LTb.GnuGo_style_Board_String());		
			
			
			//----------------------
			//genSimMove - move Processing part
			//----------------------
			if ( LTb.processMove(testmove, turn, true) ) {				 
				/*
				 * Checking for correctness
				 */				
				
				if ( turn == 1 ){ //white
					if ( sim[testmove] == 0 ) //if it was not used,
						sim[testmove] = 1;
				}
				else {//black
					if ( sim[testmove] == 0  ) //if it was not used,
						sim[testmove] = -1;	
				}
				
			//	System.out.println("\nRemoving : "+empty_positions[Lh]);
			//	System.out.println("empty_positions[empty_size] : "+empty_positions[empty_size-1]);
			//	System.out.println("empty_size : "+empty_size);
				
				empty_size--;
				empty_positions[Lh] = (empty_positions[empty_size]);
				//exchange with last one
				
				//System.out.println(LTb.GnuGo_style_Board_String());
				
				for (gLi = 0; gLi < empty_size; gLi++){
					if (LTb.getColor(empty_positions[gLi]) != 0){
						System.out.println(LTb.GnuGo_style_Board_String()
								+"\nThis point is not empty : "+empty_positions[gLi]);
						System.exit(-1);
					}						
				}
				
				if ( LTb.koPoint != -1 ) {
					empty_positions[empty_size] = ( LTb.koPoint);
					empty_size++;					
				}		
				
				else {
					for ( gLi = 0; gLi < LTb.CdCount; gLi++ ){
						empty_positions[empty_size] = (LTb.CdStones[gLi]);//  add( )koPoint;
					//	System.out.println(LTb+" "+empty_positions[empty_size]+" "+gLi);
					//	System.out.println(LTb.GnuGo_style_Board_String()+"CCC:"+LTb.CdCount);
						empty_size++;					
					}
				}
				//LegalMoves.add(LTb);//.clone());
				//if ( n == 0 ) System.out.println(n+"\n"+Tb.toString());
				
				if (LTb.koPoint != -1) {
					Lko--;
					//System.out.println("ko move: "+testmove);
					if ( Lko < 0 )
						return false;
				}
				else {
					Lko = board_size;
				}
				
			
				return true;
			}
			
			if ( Lk == 0 )
				return false;
		}
	}
	
	//Monte Carlo search from current node
	public boolean MC(UCTnode_c b, boolean not_expanded){
		//int i;
		
		//System.out.println("where:"+where+" n:"+n+" visit:"+b.visit+"\n"+b.b.GnuGo_style_Board_String()+"\n\n");
		//System.out.println("min_id:"+b.b.min_chain_id);
		Lko = board_size; //maximum number of single mc simulation ... 
		//array of empty points
		//if one of the points are captured, they are reconsidered only if that position was a ko				
		
		//LTb.copy(b.b);// = b.b.clone();
		/*for ( int ii = 0; ii < 81; ii++ )
	    	if ( !b.b.chains[ii].isempty() )
	    		System.out.println(n+"error");
		*/
		if ( not_expanded ) {	
			LTb.copy(b.parent.b);
			if ( LTb.processMove(b.from_parent_to_this_node, b.turn*-1, false) == false ){
				System.out.println("error in pmove");
				System.out.println(LTb.GnuGo_style_Board_String());
				System.out.println(b.b.GnuGo_style_Board_String());
				System.out.println(b.from_parent_to_this_node);
				System.out.println("turn : "+b.turn);
				System.out.println("n : "+n);
				System.exit(-1);
			}
			
		}
		else {
			LTb.copy(b.b);// = b.b.clone();
		}
		
		MC_iter_turn = b.turn;//mc starts from current player
		
		empty_size = 0;
		
		for ( MC_iter_i = 0; MC_iter_i < LTb.sq_boardSize; MC_iter_i++) {			
			//empty_positions[i] = new Point(i%board_size,i/board_size);
			empty_positions[empty_size] = MC_iter_i;
			
			if (LTb.getColor(empty_positions[empty_size])==0) {
				//System.out.println(empty_positions[empty_size]);
				empty_size++;				
			}			
		}
		
		//all moves as first
		if ( amaf ){
			for ( MC_iter_i = 0; MC_iter_i < LTb.sq_boardSize; MC_iter_i++) {
					sim[MC_iter_i] = 0;					
			}
		}
				
		// it is not empty for sure, 
		//1. it may not be leaf node,
		//2. if its leaf node, this function is not called
		MC_iter_i = 0;
		testmove = b.from_parent_to_this_node;
		
		while(true){	

		    if ( genSimMove(MC_iter_turn) == false ) { 
		    	MC_iter_turn = MC_iter_turn*(-1);//.opposite();
		    	if ( genSimMove(MC_iter_turn) == false )
		    		break;
		    }	
		   
		    //Mmb = LTb;//LegalMoves.get(0);
		    MC_iter_turn = MC_iter_turn*(-1);//.opposite();
		  //  System.out.println("where:"+where+" n:"+n+" visit:"+b.visit+" "+Lko+" "+i+"/"+LegalMoves.size()+"\n"+LTb);
		    if ( (LTb.capB+nw)-(LTb.capW+nb) - komi_int > 50 || (LTb.capW+nb) - (LTb.capB+nw) + komi_int > 50  ) {
		    	//not sure whether this is  ok...
		    	//System.out.println(LTb.GnuGo_style_Board_String());
		    			  break;
		    }
		    //System.out.println("min_id:"+b.b.min_chain_id);
		    //System.out.println("minid:"+LTb.min_chain_id+" "+MC_iter_i+"/"+n);
		    MC_iter_i++;
		    if ( MC_iter_i > 400 )
		    	break; //just to make it work
		    //System.out.println("\n\n"+n+" "+" "+i+"/"+LegalMoves_size+"\n"+LTb.GnuGo_style_Board_String());

		   // for ( Li = 0; Li < empty_size; Li++){
			//	System.out.print(empty_positions[Li]+" ");
			//}
		}	
		//System.out.println((LTb.capB+nw)-(LTb.capW+nb) - komi_int);
		//System.out.println("\n\n"+n+" "+" "+i+"/"+LegalMoves_size+"\n"+LTb.toString());
		//System.out.println("empty_size : " + empty_size);
		
		//System.out.println("n_iter = "+n);
		
		//we are at the end of a game here.
		//System.out.println(n+"\n"+mb.toString());
		
		UpdateTree(LTb, MadeMoves, MadeMoves_size);
		//System.out.println(mb.toString()+"\n"+n+" "+Teritory_Evaluation(mb));
		return true;
	}
	
	//Updates the tree part after MC or search is done
	// only updates Xbar, as ucb1 value is dependent on number of iterations
	public boolean UpdateTree(MBoard mb,  int [] UpdateList, int size ) {
	
		Leaf_xbar_white_turn = Teritory_Evaluation(mb); //1.0 if white wins	
		Leaf_xbar_black_turn = 1.0 - Leaf_xbar_white_turn;
		
		//System.out.println(n+" : "+Leaf_xbar_white_turn);
		//updates the nodes in the expanded tree which have been selected in current iteration
		for ( i = 0; i < size; i++ ) {//UCTnode_c mm : UpdateList ){
			
			mm = sNode[UpdateList[i]];

			//minimax update !
			// 1.0 is the score if white wins.
			
			/*
			 * this values are selected by their parents. 
			 * So they should represent the benefit 
			 * THEIR PARENTS will have when CHOOSING them.
			 * 
			 */	
			
			if ( mm.turn == -1 ){//Color.BLACK ) { 
				Leaf_xbar = Leaf_xbar_white_turn;
				//System.out.println(mm.b.toString()+mb.toString()+turn.toString()+mm.turn.toString()+"\n");
			}
			// 0.0 is the score if black wins
			else if (mm.turn==1)
				Leaf_xbar = Leaf_xbar_black_turn;
			else {
				Leaf_xbar = 0;
				System.out.println("Error in turn value");
				System.exit(-1);
			}
						
		//	UpdateList[i].xbar = ( Leaf_xbar+ 
			//		((UpdateList[i].visit-1.0) * UpdateList[i].xbar) ) / (UpdateList[i].visit);			
			
			//System.out.println("\n 1xbar:"+UpdateList[i].xbar);
			
			mm.xbar = mm.xbar
						+((Leaf_xbar-mm.xbar)/ (mm.visit));
			
			//System.out.println(" 2xbar:"+UpdateList[i].xbar+"\n visit :"+UpdateList[i].visit);
			
			//if ( mm.b != null )
			//	System.out.println(mm.b.GnuGo_style_Board_String());
			//System.out.println(" xbar:"+mm.xbar+" mm.visit:"+mm.visit);
			
			//value used by UCB1-TUNED algorithm ( see  Gelly 2006, P.Auer, 2002 )			
			//mm.square_sum_xbar += Leaf_xbar*Leaf_xbar;
			
			//amaf heuristic & if the UCT node has at least one child,
			
			if ( amaf && (mm.child_count != 0) ){ 
			//	System.out.println(mm.child_count);
				if (mm.turn == 1) {//child is black 
					
					for( UT_iterator = 0; UT_iterator < mm.child_count-1; UT_iterator++ ){						
						if (sim[sNode[mm.child[UT_iterator]].from_parent_to_this_node]==1) { 
							sNode[mm.child[UT_iterator]].amaf_visit += 1.0;
							sNode[mm.child[UT_iterator]].amaf_xbar_sum += Leaf_xbar_white_turn;
						//	System.out.println("case w_in");
							//System.out.println(mm.amaf_visit+" "+mm.amaf_xbar_sum);
						}						
					}
					//System.out.println("case w");
				}
				else { //child is white
					for( UT_iterator = 0; UT_iterator < mm.child_count-1; UT_iterator++ ){
						if (sim[sNode[mm.child[UT_iterator]].from_parent_to_this_node]==-1) {  
							sNode[mm.child[UT_iterator]].amaf_visit += 1.0;
							sNode[mm.child[UT_iterator]].amaf_xbar_sum += Leaf_xbar_black_turn;
						//	System.out.println("case b_in");
						}
					}
				//	System.out.println("case b");
				}
			}
		}
		return true;
	}
	
	//Updates the child of each node before selecting them
	// essentially, this part updates ucb1 value. 
	public boolean UpdateChild(UCTnode_c  UpdateList ) {
		
		//This one was used before...... 
		//log_n = Math.sqrt(2.0*(double)n); //calculated once here
		//wt_param = Math.sqrt(amaf_k/(3.0*(double)n+amaf_k));
		double this_n = UpdateList.visit ;
		//calculated once here
		log_n = ratio*Math.sqrt(Math.log(this_n)); 		
		wt_param = Math.sqrt(amaf_k/(3.0*this_n+amaf_k));
		nwt_param = 1.0-wt_param;
		//System.out.println("\nn : "+n+" "+"visit : "+UpdateList.visit+"fptt = "+UpdateList.from_parent_to_this_node);
		

		//updates the nodes in the expanded tree which have been selected in current iteration
		for ( UC_i=0; UC_i < UpdateList.child_count; UC_i++){// mm : UpdateList ){
			//UC_mm = sNode[UpdateList.child[UC_i]];
			if (sNode[UpdateList.child[UC_i]].visit < 0.9 ) {
				/*
				UC_mm.ucb1 = 100; //never happens, or should not happen
				*/
				if ( sNode[UpdateList.child[UC_i]].ucb1  < 100 ) {
					System.out.println("update error:"+sNode[UpdateList.child[UC_i]].ucb1);
					System.exit(1);
				}
				/*
				if (amaf) {
					System.out.println("EMPTY : "+sNode[UpdateList.child[UC_i]].from_parent_to_this_node +
							"\tucb1 : "+sNode[UpdateList.child[UC_i]].ucb1 + 
							"\tamafvisit : "+sNode[UpdateList.child[UC_i]].amaf_visit);		
					
				}*/
				
			}
			else { 				
				//One can choose UCB1 here
				
				//------------------------
				//plain UCB1
				//------------------------				
				sNode[UpdateList.child[UC_i]].ucb1 = sNode[UpdateList.child[UC_i]].xbar; 
				//System.out.print("xbar = "+UC_mm.ucb1);
				
				if ( amaf  ){/*
					System.out.print("fptt = "+sNode[UpdateList.child[UC_i]].from_parent_to_this_node);
					System.out.print("\tvisit = "+sNode[UpdateList.child[UC_i]].visit);
					System.out.print("\tanafvisit = "+sNode[UpdateList.child[UC_i]].amaf_visit);
					System.out.println("\txbar = "+sNode[UpdateList.child[UC_i]].ucb1);
					*/
					//if ((n % 20 != 0)&&) {
						// skip RAVE ( can be only on RAVE ... but Feugo say
						//'only on mean value', so this should be the right way.
						//This didn't worked out well. ( see Rd4 -> Rd5 )					
						//Skip rave is now disabled
//					}
						
					//System.out.print("ori:"+UC_mm.ucb1);
					sNode[UpdateList.child[UC_i]].ucb1 = nwt_param*sNode[UpdateList.child[UC_i]].ucb1;
					if ( sNode[UpdateList.child[UC_i]].amaf_visit > 0.0 ) {
						sNode[UpdateList.child[UC_i]].ucb1 += 
							(
							wt_param*
							(sNode[UpdateList.child[UC_i]].amaf_xbar_sum/
									sNode[UpdateList.child[UC_i]].amaf_visit));
				//		System.out.print("\twt:"+wt_param);
				//		System.out.print("\tnwt:"+nwt_param);
						//System.out.println("n: "+n+"\tamf:"+wt_param);
						//System.out.println("\tamf:"+wt_param*(UC_mm.amaf_xbar_sum/UC_mm.amaf_visit));
					}
					//else
					//	System.out.println("0");
				//	System.out.print("\tRAVE = "+sNode[UpdateList.child[UC_i]].ucb1);
					
				//	System.out.print("\texpterm:"+log_n*Math.sqrt(1.0/(UC_mm.visit)));
					sNode[UpdateList.child[UC_i]].ucb1 += 
						log_n*Math.sqrt(1.0/(sNode[UpdateList.child[UC_i]].visit));
					//System.out.println("\tucb1 = "+	log_n*Math.sqrt(1.0/(sNode[UpdateList.child[UC_i]].visit)));
				
				//	System.out.println("\tRAVE UCT = "+sNode[UpdateList.child[UC_i]].ucb1);
					
				}
				else {
					
					sNode[UpdateList.child[UC_i]].ucb1 += 
						log_n*Math.sqrt(1.0/(sNode[UpdateList.child[UC_i]].visit));
					
				//	System.out.println("\tUCT = "+sNode[UpdateList.child[UC_i]].ucb1);
					//System.out.println("\tucb1 = "+UC_mm.ucb1);
				}
				
				
				
				
				//------------------------

				//------------------------
				//UCB1-TUNED algorithm ( see Gelly 2006 (first Mogo paper),  P.Auer, 2002 )
				//------------------------
				/*
				V_j =   ( mm.square_sum_xbar / (double) mm.visit ) 
				     -  ( mm.xbar*mm.xbar )
				     +  ( Math.sqrt( ( 2.0/(double)mm.visit ) * Math.log((double)n)) );
				if ( 0.25 
						> V_j
				)
				{
					mm.ucb1 = (ratio* mm.xbar)+Math.sqrt(V_j*Math.log((double)n)/(double)mm.visit);				
				}
				else
					mm.ucb1 = (ratio* mm.xbar)+Math.sqrt(0.25*Math.log((double)n)/(double)mm.visit);	
					*/
				//------------------------
			}
			//System.out.println("mm.ucb1 : "+UC_mm.ucb1);
		}
		return true;
	}
	
	//Gives information about who win the game at the end of game
	//current implementation
	//1. number of captured stones
	//2. number of stones on the board
	//3. if the position is empty, counts nearest points
	public double Teritory_Evaluation( MBoard mb ){
		
		//This version calculates 'Territory' 
		// 1. all the dead stones and captured stones
		// 2. empty positions, that belongs to one color
				
		nb=0;
		nw=0;
		for (Ti = 0; Ti < mb.sq_boardSize; Ti++){
			if (!mb.chains[Ti].isempty()) {
				if (mb.chains[Ti].inAtari()){ 
					if ( mb.getColor(mb.chains[Ti].member[0]) == -1 ) {//black
						nw += mb.chains[Ti].chain_size;
						nw += mb.chains[Ti].chain_size;
					}
					else if ( mb.getColor(mb.chains[Ti].member[0]) == 1 ){//white
						nb += mb.chains[Ti].chain_size;
						nb += mb.chains[Ti].chain_size;
					}
				}				
			}
		}
		//for black or white stones 
		/*
		for (Ti = 0; Ti < mb.sq_boardSize; Ti++){
			if (!mb.chains[Ti].isempty()) {
				if (!mb.chains[Ti].inAtari()){ 
					if ( mb.getColor(mb.chains[Ti].member[0]) == -1 )//black
						nb += mb.chains[Ti].chain_size;
					else if ( mb.getColor(mb.chains[Ti].member[0]) == 1 )//white
						nw += mb.chains[Ti].chain_size;
				}
				else {
					//if the chains is dead, add it to opponent's score
					if ( mb.getColor(mb.chains[Ti].member[0]) == -1 )//black
						nw += mb.chains[Ti].chain_size;
					else if ( mb.getColor(mb.chains[Ti].member[0]) == 1 )//white
						nb += mb.chains[Ti].chain_size;
				}
			}
		}
		*/
		//For empty intersections
		
		//This is not the complete version,
		// let's just assume all the dead stones are captured during the simulation ......
		//as black and white stones are all covered, we now deal with empty intersections			
		for (Ti = 0; Ti < mb.sq_boardSize; Ti++){
			if (mb.getColor(Ti)==0)
				Tempty[Ti]= true;
			else
				Tempty[Ti]= false;
			
			//Tva[Ti] = 0; 
			//Teb[Ti] = 0;
		}
		//basically, we do flooding		
		
		//Tk
		//0:empty
		//1:black
		//-1:white
		//2 : error, meaning this empty intersection chain does not belong to either color
		//Tvn = 0;
		//Tk = 0;
		for (Ti = 0; Ti < sq_board_size; Ti++){			
			if (Tempty[Ti]){
				Tempty[Ti] = false;
				Tva[0] = Ti;
				Tk = 0;				
				Tvn = 1;
				for (Tj = 0; Tj < Tvn; Tj++ ){					
						
					//up
					Tl = Tva[Tj] + board_size;
					if (Tl < sq_board_size) {
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = mb.getColor(Tl);
							else if (Tk != mb.getColor(Tl) && mb.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
					//down
					Tl = Tva[Tj] - board_size;
					if (Tl > 0) {
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = mb.getColor(Tl);
							else if (Tk != mb.getColor(Tl) && mb.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
					//Left					
					if (Tva[Tj] % board_size != 0 ) {
						Tl = Tva[Tj] -1;
						
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = mb.getColor(Tl);
							else if (Tk != mb.getColor(Tl) && mb.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
					//Right
					if (Tva[Tj] % board_size != board_size -1) {
						Tl = Tva[Tj] + 1;
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = mb.getColor(Tl);
							else if (Tk != mb.getColor(Tl) && mb.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
				}
				/*
				if (Tk != 2 && Tvn > 1){
					System.out.println(mb.GnuGo_style_Board_String());
					System.out.println(Tvn);
					System.out.println(Tk);
					System.out.println(Tva[0]);
				}
				*/
				if (Tk == -1)
					nb+=Tvn;
				else if ( Tk == 1)
					nw += Tvn;					
			}
		}
		
		
		if (mb.capB+nw+komi_int >= mb.capW+nb) { // 5.5 as komi
			
			return 1.0 - (rgen.nextDouble()/10000.0);
		}
		else if (mb.capB+nw == mb.capW+nb){
			
			return 0.5+ (rgen.nextDouble()/10000.0);
		}
		else {
			
			return 0.0+ (rgen.nextDouble()/10000.0);
		}	
			
	}
	
	//Expensive version of Teritory_Evaluation.
	//Extends Territory evaluator to check the live/dead points...
	//used only for deciding pass, when the opponent passes. 
	
	public double Expensive_Teritory_Evaluation( MBoard mb, int turn ){		

		//we do random playout from this node and see which chains are dead at the end .
		//Then we are going to remove all the dead chains
		//After dead chains are removed, we count the territory
		
		for ( Ti = 0; Ti < sq_board_size;  Ti++ )
			ETEChaincounter[Ti] = 0;
		
		DA = mb.clone();

		
		for ( Tj = 0; Tj < 10000; Tj++ ) {
			Lko = board_size; //maximum number of single mc simulation ... 		
			MC_iter_turn = turn;			
			LTb.copy(mb);// = b.b.clone();		
			empty_size = 0;
			
			for ( MC_iter_i = 0; MC_iter_i < LTb.sq_boardSize; MC_iter_i++) {
				
				
				empty_positions[empty_size] = MC_iter_i;			
				
				if (LTb.getColor(empty_positions[empty_size])==0)
					empty_size++;			
			}		
			
			MC_iter_i = 0;
			testmove = -1;
			while(true){	
			    if ( genSimMove(MC_iter_turn) == false ) { 
			    	MC_iter_turn = MC_iter_turn*(-1);//.opposite();
			    	if ( genSimMove(MC_iter_turn) == false )
			    		break;
			    }			    
			    MC_iter_turn = MC_iter_turn*(-1);//.opposite();

			    MC_iter_i++;
			    if ( MC_iter_i > 400 )
			    	break; //just to make it work
			}	
			
			//System.out.println(LTb.GnuGo_style_Board_String());
			

			
			/*
			 * We check whether the chain is alive or not
			 */
			
			for (Ti = 0; Ti < mb.sq_boardSize; Ti++){
				if (!DA.chains[Ti].isempty()) {
					for (Tk = 0; Tk < DA.chains[Ti].chain_size; Tk++ ){
						if( DA.getColor(DA.chains[Ti].member[Tk]) != 
							LTb.getColor(DA.chains[Ti].member[Tk])){
							//chain is dead
							ETEChaincounter[Ti]++;
							break;
							//Tj = sq_board_size;
						}
					}
						/*
					if (LTb.chains[Ti].isempty() || 
							(DA.chains[Ti].chain_size > LTb.chains[Ti].chain_size) ||
							(DA.chains[Ti].member[0] != LTb.chains[Ti].member[0]) ) {					
						//chain is dead in this simulation
						//we add this information
						ETEChaincounter[Ti]++;
					}		
					*/		
				}
			}
		}
		nb=0;
		nw=0;	
		
		//we manipulate DA from below
		
		for (Ti = 0; Ti < mb.sq_boardSize; Ti++){
			if (!DA.chains[Ti].isempty()) {
				if ( ETEChaincounter[Ti] > 3000 ) { 
					// if the chain dies more than 1/3 of the simulation,
					// there is high probability that it is dead. 
					
					//chain is dead
					//we remove this
					if ( DA.getColor(DA.chains[Ti].member[0]) == -1 ) {//black
						nw += DA.chains[Ti].chain_size; // adds prisoners						
						for (int eti = 0; eti < DA.chains[Ti].chain_size; eti++){
							DA.CaptureProcess(DA.chains[Ti].member[eti], 1);
						}
						DA.chains[Ti].chain_size = 0;
					}
					else if ( DA.getColor(DA.chains[Ti].member[0]) == 1 ){//white
						nb += DA.chains[Ti].chain_size; // adds prisoners
						for (int eti = 0; eti < DA.chains[Ti].chain_size; eti++){
							DA.CaptureProcess(DA.chains[Ti].member[eti], -1);
						}						
						DA.chains[Ti].chain_size = 0;
					}
				}				
			}
		}
		
		//System.out.println("-------------\n"+DA.GnuGo_style_Board_String()+"\n----------------");
		
		//For empty intersections		
		//This is not the complete version,
		// let's just assume all the dead stones are captured during the simulation ......
		//as black and white stones are all covered, we now deal with empty intersections			
		for (Ti = 0; Ti < mb.sq_boardSize; Ti++){
			if (DA.getColor(Ti)==0)
				Tempty[Ti]= true;
			else
				Tempty[Ti]= false;
		}
		//basically, we do flooding		
		
		//Tk
		//0:empty
		//1:black
		//-1:white
		//2 : error, meaning this empty intersection chain does not belong to either color
		//Tvn = 0;
		//Tk = 0;
		for (Ti = 0; Ti < sq_board_size; Ti++){			
			if (Tempty[Ti]){
				Tempty[Ti] = false;
				Tva[0] = Ti;
				Tk = 0;				
				Tvn = 1;
				for (Tj = 0; Tj < Tvn; Tj++ ){					
						
					//up
					Tl = Tva[Tj] + board_size;
					if (Tl < sq_board_size) {
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = DA.getColor(Tl);
							else if (Tk != DA.getColor(Tl) && DA.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
					//down
					Tl = Tva[Tj] - board_size;
					if (Tl > 0) {
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = DA.getColor(Tl);
							else if (Tk != DA.getColor(Tl) && DA.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
					//Left					
					if (Tva[Tj] % board_size != 0 ) {
						Tl = Tva[Tj] -1;
						
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = DA.getColor(Tl);
							else if (Tk != DA.getColor(Tl) && DA.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
					//Right
					if (Tva[Tj] % board_size != board_size -1) {
						Tl = Tva[Tj] + 1;
						if (Tempty[Tl]) {
							Tempty[Tl] = false;
							Tva[Tvn] = Tl;
							Tvn++;
						}
						else if (Tk != 2) {
							if (Tk == 0)
								Tk = DA.getColor(Tl);
							else if (Tk != DA.getColor(Tl) && DA.getColor(Tl) != 0)
								Tk = 2;			
						}
					}
					
				}
				//System.out.println("Tk = "+Tk+"Tvn = "+Tvn);
				
				
				if (Tk == -1)
					nb+=Tvn;
				else if ( Tk == 1)
					nw += Tvn;					
			}
		}		
		
		if (mb.capB+nw+komi_int >= mb.capW+nb) { // 5.5 as komi
			
			return 1.0 + rgen.nextDouble()/10000.0;
		}
		else if (mb.capB+nw == mb.capW+nb){
			
			return 0.5+ rgen.nextDouble()/10000.0;
		}
		else {
			
			return 0.0+ rgen.nextDouble()/10000.0;
		}				
	}
	
	
	//Prints an approximate final score ...... 
	public double final_score(MBoard cb, int turn){
		//more correct... version
		Expensive_Teritory_Evaluation( cb, turn );
		
		/*
		//Area scoring
		if ( turn >0 ) //turn : white
			return (double)((nw)-(nb))+komi;
		else //turn : black
			return (double)((nb)-(nw))-komi;
		*/
		 
		//Territory scoring 
		//System.out.println(cb.capB+" "+cb.capW+" "+nw+" "+nb);
		
		if ( turn >0 ) //turn : white
			return (double)((cb.capB+nw)-(cb.capW+nb))+komi;
		else //turn : black
			return (double)((cb.capW+nb)-(cb.capB+nw))-komi;
			
	}
}
	
