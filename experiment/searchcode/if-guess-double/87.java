package tests.pojos;

import java.util.Random;

public class CoinFlip {
	double headHits = 0;
	double tailHits = 0;
	int nomOfTrials = 0;
	double guessesRight = 0;
	Random rand = new Random();
	
	public CoinFlip(){}
	
	public void runTrials(int nomOfTrials){
		this.nomOfTrials = nomOfTrials;
		for(int i = 0; i < nomOfTrials; i++){
			int guess = rand.nextInt(2);
			int actual = rand.nextInt(2);
			
			if(guess == actual){
				guessesRight++;
			}
			
			if(actual == 0){
				headHits++;
			}else{
				tailHits++;
			}
		}
	}
	
	public void printTrials(){
		System.out.println("Tails: " + ((tailHits / nomOfTrials) * 100) +
				"\nHeads: " + ((headHits / nomOfTrials) * 100) +
				"\nGuesses right: " + ((guessesRight / nomOfTrials) * 100));
	}
	
	public static void main(String... args){
		CoinFlip cf = new CoinFlip();
		cf.runTrials(1000000);
		cf.printTrials();
	}
}

