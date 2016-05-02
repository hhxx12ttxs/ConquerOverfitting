package pentago.model; 

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pentago.constants.Numbers;
import pentago.game.Game;


/**
 * Deze klasse representeert een bord waarop gespeeld kan
 * worden. Elk Board bevat precies negen blokken (Block)
 * die elk gedraaid kunnen worden.
 *
 * Een bord wordt onder andere gebruikt in Server en Client
 * om de state van een spel bij te houden.
 *
 */
public class Board {
	private Block[] blocks;
	
	public static final int DIM = 3;
	public static final int DIM2 = DIM * DIM;
	public static final int ROWLENGTH = DIM * Numbers.BLOCK_DIM;

	/*
	 * Initialiseer een bord-object. 
	 */
	public Board(){
		this.blocks = new Block[Board.DIM2];
		for (int i=0; i < Board.DIM2; i++){
			this.blocks[i] = new Block();
		}
	}
	
	/**
	 * Vraag Block object met nummer 'blocknr' op
	 *
	 * @require: 0 <= blocknr <= 8
	 * @ensure: return != null
	 * @return model.Block
	 */
	public Block getBlock(int blocknr){
		return this.blocks[blocknr];
	}

	/**
	 * Vraag Block object op door middel van een
	 * tilenr.
	 *
	 * @require 0 <= tilenr <= 80
	 * @return models.Player of null
	 */
	public Block getBlockByTile(int tilenr){
		return this.getBlock(tilenr / Board.DIM2);
	}


	/**
	 * Vraag vakje op uit het bord. Deze methode
	 * resolved tilenr naar een block en haalt vanuit
	 * daar het goede vakje op.
	 *
	 * @require: 0 <= tilenr <= 80
	 * @return: models.Player of null
	 */
	public Player getTile(int tilenr){
		Block bl = this.getBlock(tilenr / Board.DIM2);
		return bl.getTile(tilenr % Board.DIM2);
	}

	/**
	 * Vul het vakje in met de speler. Nummer wordt dus automatisch vertaald naar het goed Block
	 * @param tilenr Nummer van het vakje9
	 * @param play Player waarmee het vakje gevuld moet worden.
	 * @require 0<=tilenr<=80
	 * @ensure this.getTile(tilenr).equals(play)
	 */
	public void setTile(int tilenr, Player play){
		this.getBlock(tilenr/Board.DIM2).setTile(tilenr%Board.DIM2, play);
	}
	
	/**
	 * 
	 * 
	 * @param row
	 * @return
	 */
	private Player getWinner(List<Integer> row){
		Player curr = null; 
		
		int count = 0;
		for (int i=0; i < row.size(); i++){
			// Is de vorige speler gelijk aan de huidige?
			if (this.getTile(row.get(i)) != curr){
				curr = this.getTile(row.get(i));
				count = 0;
			}
			
			count++;
			
			// Break wanneer er een winnende speler is gedetecteerd
			if (curr != null && count >= Numbers.WINNING_AMOUNT){
				return curr;
			}
		}
		
		return null;
	}
	
	/**
	 * Geeft een lijst met winnaars terug op basis van het huidige
	 * bord 
	 * 
	 * @return lijst met winnaars
	 */
	public Set<Player> getWinners(){
		Set<Player> winners = new HashSet<Player>();
		
		// Controleer verticale rijen
		for (int i=0; i < ROWLENGTH; i++){
			winners.add(this.getWinner(Board.getColumnIndices(i)));
			winners.add(this.getWinner(Board.getRowIndices(i)));
		}
		
		// Controleer diagonalen
		int diagonals = (4 * (ROWLENGTH)) - 1;
		for (int i=0; i < diagonals; i++){
			winners.add(this.getWinner(Board.getDiagonalIndices(i)));
		}
		
		winners.remove(null);		
		return winners;
	}
	
	/**
	 * Geeft aan of er een winnaar is
	 * @return true als er een of meerdere winnaars zijn, anders false
	 */
	public boolean hasWinner(){
		return this.getWinners().size() > 0;
	}
	
	/**
	 * Geeft het tilenummer terug op basis van een geleverde rij en kolom
	 * 
	 * @param row rijnummer
	 * @param col kolomnummer
	 */
	public static int getTileNr(int row, int col){
		int tile = 0;

		// Daar moeten we een getal aan toevoegen, omdat we verder
		// opschuiven naar rechts
		tile += col % Numbers.BLOCK_DIM;
		tile += (col / Numbers.BLOCK_DIM) * Numbers.BLOCK_DIM2;
		
		// En hetzelfde geldt voor het geval dat we naar beneden
		// dalen
		tile += Numbers.BLOCK_DIM * (row % Numbers.BLOCK_DIM);
		tile += Numbers.BLOCK_DIM2 * Numbers.BLOCK_DIM * (row / Numbers.BLOCK_DIM);
		
		return tile;
	}
	
	/**
	 * Geeft een hele rij terug
	 * @param row De index van de rij
	 * @return Een List<Player> met de bezetting van de Tiles
	 * @require 0 <= row <= 8
	 * @ensure result != null
	 * @ensure result.length == ROWLENGTH
	 */
	public static List<Integer> getRowIndices(int row){
		// Het resultaat is een list met integers die indices voorstellen
		List<Integer> indices = new ArrayList<Integer>(); 
		
		for (int col=0; col < (ROWLENGTH); col++){
			indices.add(Board.getTileNr(row, col));
		}
		
		return indices;
	}
	
	/**
	 * Geeft een hele kolom terug.
	 * @param col De index van de kolom
	 * @return Een List<Player> met de bezetting van de Tiles
	 * @require 0 <= col <= 9
	 * @ensure result != null
	 * @ensure result.size() == 9
	 */
	public static List<Integer> getColumnIndices(int col){
		List<Integer> indices = new ArrayList<Integer>();
		
		for (int row=0; row < (ROWLENGTH); row++){
			indices.add(Board.getTileNr(row, col));
		}
		
		return indices;
	}
	
	/**
	 * Geeft een diagonaal terug. Zie hieronder voor index:
	 * 
	 * Originele bord:          Diagonaal indices (I) [NorthWest]:
	 * 
	 *  00 | 01 || 04 | 05       00 | 01 || 02 | 03 
	 * ----+----++----+----     ----+----++----+----
	 *  02 | 03 || 06 | 07       01 | 02 || 03 | 04 
	 * =========++=========     =========++=========
	 *  08 | 09 || 12 | 13       02 | 03 || 04 | 05 
	 * ----+----++----+----     ----+----++----+----
	 *  10 | 11 || 14 | 15       03 | 04 || 06 | 07
	 *  
	 *  
	 * Diagonaal indices (II) [NorthEast]
	 * 
	 *  11 | 10 || 09 | 08 
	 * ----+----++----+----
	 *  12 | 11 || 10 | 09 
	 * =========++=========
	 *  13 | 12 || 11 | 10 
	 * ----+----++----+----
	 *  14 | 13 || 12 | 11	
	 * 
	 * @param index zie hierboven
	 * @return Een List<Integer> met de indices die bij een bepaalde diagonaal horen
	 */
	public static List<Integer> getDiagonalIndices(int index){
		List<Integer> indices = new ArrayList<Integer>();
		
		// Maximun index in het geval van NW
		int nwMax = (ROWLENGTH - 1) * 2;
		
		// Gebruikt in loop
		int row = 0, col = index, diff;
		
		// Initialiseer col, row waarden
		if(index > nwMax){
			row = index - nwMax - 1;
			col = ROWLENGTH - 1;
		}
		
		// Corrigeer te grote row, col waarden
		if (col >= ROWLENGTH){
			diff = Math.abs((ROWLENGTH - 1 - col));
			
			col = col - diff;
			row = row + diff;
		} else if (row >= ROWLENGTH){			
			col = col + (ROWLENGTH - 1 - row);
			row = ROWLENGTH - 1;
		}
		
		// Fill result list
		while (row >= 0 && col >= 0 && row < ROWLENGTH && col < ROWLENGTH){
			indices.add(Board.getTileNr(row, col));
			
			row = (index > nwMax) ? row - 1 : row + 1;
			col--;
		}
		
		return indices;
	}
	
	public static void main(String[] args){
		System.out.println(Board.getDiagonalIndices(33));
	}
}

