//<><><><><><><><><><><><><>
//Programming Assignment 4
//Olivia Winn
//oaw2102
//<><><><><><><><><><><><><>

//<><><><><><><><><>
//Part 1: Blackjack
//<><><><><><><><><>

import java.util.ArrayList;

public class Dealer{
	
	ArrayList<Card> hand = new ArrayList<Card>();
	boolean bust = false;
	boolean blackjack = false;
	
	public Dealer(){
		
	}
	
	public void addCard(Card dealt){
		Card cardDealt = dealt;
		hand.add(cardDealt);
	}
	
	public Card dealerCard(){
		return hand.get(0);
	}
	
	public boolean hit() {
		if (evaluate() < 17)
			return true;
		else
			return false;
	}
	
	public int evaluate(){
		if (hand.get(0).value() == 0){
			if (hand.get(1).value() == 10 || hand.get(1).value() == 11 || hand.get(1).value() == 12){
				blackjack = true;
			}	
		}
		else if (hand.get(0).value() == 10 || hand.get(0).value() == 11 || hand.get(0).value() == 12){
			if (hand.get(1).value() == 0){
				blackjack = true;
			}
		}
		
		int sum = 0;
		int ace = 0;
		
		if (blackjack == true){
			sum = 21;
		}
		else{
			for (int i = 0; i < hand.size(); i++){
				int value = hand.get(i).value();
				if (value == 11 || value == 12 || value == 13){
					value = 10;
				}
				else if (value == 1){
					value = 11;
				}
				sum += value;
			}
			
			while (sum > 21 && ace > 0){
				sum = sum - 10;
				ace --;
			}
			
			if (sum > 21)
				bust = true;
		}
		return sum;
	}
	
	public boolean blackjack(){
		return blackjack;
	}
	
	public int hand(){
		return hand.size();
	}
	
	public void clearHand(){
		for (int i = hand.size() - 1; i >= 0; i--)
			hand.remove(i);
	}
	
}

