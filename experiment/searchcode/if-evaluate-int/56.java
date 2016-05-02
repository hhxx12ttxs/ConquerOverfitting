//<><><><><><><><><><><><><>
//Programming Assignment 4
//Olivia Winn
//oaw2102
//<><><><><><><><><><><><><>

//<><><><><><><><><>
//Part 1: Blackjack
//<><><><><><><><><>

import java.util.ArrayList;
import java.util.Scanner;

public class Player{
	
	ArrayList<Card> hand = new ArrayList<Card>();
	boolean bust = false;
	boolean split = false;
	boolean blackjack = false;
	
	Scanner input = new Scanner(System.in);
	
	public Player(){
		
	}
	
	public void addCard(Card dealt){
		Card cardDealt = dealt;
		hand.add(cardDealt);
	}
	
	public int firstMove(){
		System.out.println(cards());
		String move = "Enter 1 if you would like to double down.";
		if (hand.get(0).value() == hand.get(1).value())
			move += "Enter 2 if you would like to split.";
		move += "\nIf you want to skip this choice, enter 3.";
		System.out.println(move);
		int choice = input.nextInt();
		return choice;
	}
	
	public Card split(){
		Card holder = hand.get(1);
		hand.remove(1);
		split = true;
		return holder;
	}
	
	public boolean hit() {
		System.out.println(cards());
		
		int choice = 0;
		if (blackjack()){
			System.out.println(hand);
			return false;
		}
		else if (evaluate() >= 21){
			return false;
		}
		else{
			System.out.println("Would you like to hit or stand? Pick 1 for hit and 2 for stand.");
			choice = input.nextInt();
			if (choice == 2)
				return false;
			else
				return true;
		}
		
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
		
		if (split == true)
			blackjack = false;
		
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
					ace ++;
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
	
	public String cards(){
		String cards = "Your hand:\n";
		for (int i = 0; i < hand.size(); i++)
			cards += hand.get(i).toString() + ", ";
		cards += "\nThe sum: " + Integer.toString(evaluate());
		
		return cards;
	}

	

}

