package com.clinkworks.games.pinochle;

import java.util.List;

import com.clinkworks.carddeck.Card;
import com.clinkworks.carddeck.base.CardCollection;
import com.clinkworks.carddeck.datatypes.Suit;
import com.clinkworks.decks.PinochleCard;
import com.clinkworks.decks.PinochleCardDeck;
import com.clinkworks.games.components.Player;
import com.clinkworks.games.pinochle.components.PinochleTable;



public class Main {
	public static void main(String[] args){
		
		PinochleTable table = new PinochleTable(
				new Player("Player1"), 
				new Player("Player2"), 
				new Player("Player3"), 
				new Player("Player4"));
		
		PinochleCardDeck deck = PinochleCardDeck.buildDeckSingleDeck().shuffle();
		
		table.setTrump(Suit.Hearts);
		
		//test sort and comparable order, order from least to greatest.
		int i = 0;
		for(PinochleCard card : CardCollection.sortCards(deck)){
			System.out.print(card.getShorthandText() + " ");
			if(i < 5){
				i++;
			}else{
				System.out.print("\n");
				i = 0;
			}
		}

	}
}

