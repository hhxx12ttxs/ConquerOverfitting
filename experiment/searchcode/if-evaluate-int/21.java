//<><><><><><><><><><><><><>
//Programming Assignment 4
//Olivia Winn
//oaw2102
//<><><><><><><><><><><><><>

//<><><><><><><><><>
//Part 1: Blackjack
//<><><><><><><><><>

import java.util.Scanner;

public class Game{
	
	private Deck d;
	private Player p1;
	private Dealer p2;
	private boolean split = false;
	private Player p3;
	private boolean rounds = true;
	private Scanner input = new Scanner(System.in);
	private double cash = 100;
	private double bet;
	private boolean shuffle = false;

	private int cardsInHand = 0;
	
	public Game(){
		d = new Deck(1);
		p1 = new Player();
		p2 = new Dealer();
		System.out.println("Welcome to Blackjack! Buy-in is $100.");
		
	}
	
	public void play(){
		
		while (rounds){
			System.out.println("Make a bet between $10 and $1000!");
			bet = input.nextInt();
			if (bet < 10 || bet > 1000){
				System.out.println("You can't bet that amount of money. Try again.");
				bet = input.nextInt();
			}
			cash = cash - bet;
		
			if (d.cardsDealt() >= 40 || d.cardsDealt() == 0)
			d.Shuffle(0);
			for (int i = 0; i < 2; i ++){
				p1.addCard(d.deal());
				p2.addCard(d.deal());
				cardsInHand += 2;
			}
		
			System.out.println("The dealer's card is a " + p2.dealerCard());
			
			if (d.cardsDealt() >= 40){
				d.Shuffle(cardsInHand);
				shuffle = true;
			}
			
			p1.evaluate();
			if (!p1.blackjack){
				int choice = p1.firstMove();
				if (choice != 3){
					bet = bet * 2;
					cash = cash - bet;
					if (choice == 2){
						split();
						split = true;
					}
				}
				
			}
		
			if (d.cardsDealt() >= 40){
				d.Shuffle(cardsInHand);
				shuffle = true;
			}
		
			player1();
			if (split)
				player3();
			player2();
			evaluate();
		
			System.out.println("Would you like to play again? 1 for yes and 2 for no.");
			int choice = input.nextInt();
			if (choice == 2){
				rounds = false;
				System.out.println("Thanks for playing!");
			}
		}
	}
	
	private void split(){
		p3 = new Player();
		p3.addCard(p1.split());
		p1.addCard(d.deal());
		if (d.cardsDealt() >= 40){
			d.Shuffle(cardsInHand);
			shuffle = true;
		}
	}
	
	private void player1(){
		while (p1.hit()){
			System.out.println("The dealer's card is a " + p2.dealerCard());
			p1.addCard(d.deal());
			if (d.cardsDealt() == 40){
				d.Shuffle(cardsInHand);
				shuffle = true;
			}
		}
	}
	
	private void player2(){
		while (p2.hit()){
			p2.addCard(d.deal());
			if (d.cardsDealt() == 40){
				d.Shuffle(cardsInHand);
				shuffle = true;
			}
		}
	}
	
	private void player3(){
		while (p3.hit()){
			p3.addCard(d.deal());
			if (d.cardsDealt() == 40){
				d.Shuffle(cardsInHand);
				shuffle = true;
			}
		}
	}
	
	private void evaluate(){
		String outcome;
		int p1Score = p1.evaluate();
		if (split){
			int p3Score = p3.evaluate();
			if ((!p3.bust) && p3Score > p1Score){
				p1Score = p3Score;
				p3.clearHand();
			}
		}
		if (p1.bust){
			outcome = "You busted! The dealer wins.";
		}
		else{
			int p2Score = p2.evaluate();
			if (p2.bust){
				outcome = "The dealer busted. You win!";
				cash += (bet * 2);
			}
			else if (p1.blackjack && p2.blackjack)
				outcome = "The game was a tie.";
			else if (p1.blackjack){
				outcome = "You won with a blackjack! Congratulations!";
				cash += (bet * (3/2));
			}
			else if (p2.blackjack)
				outcome = "The dealer got a blackjack, you lost.";
			else if (p1Score == p2Score){
				outcome = "The dealer got the same score with " + p2.hand() + " cards,";
				if (p1.hand() < p2.hand()){
					outcome += " and you won with fewer cards!";
					cash += (bet * 2);
				}
				else if (p1.hand() > p2.hand())
					outcome += " and you had more cards. Sorry, you lost.";
				else
					outcome += "\nThe game was a tie.";
			}
			else if (p1Score > p2Score){
				outcome = "The dealer got a score of " + p2Score + ". You won!";
				cash += (bet * 2);
			}
			else
				outcome = "The dealer got a score of " + p2Score + ". The dealer won.";
		}
		
		outcome += "\nYou now have $" + cash + ".";
		System.out.println(outcome);
		bet = 0;
		cardsInHand = 0;
		p1.clearHand();
		p2.clearHand();
		if (shuffle)
			d.Shuffle(0);
	}

}

