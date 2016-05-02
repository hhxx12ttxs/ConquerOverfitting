package geneticAlgorithm;
import java.util.Arrays;
import java.util.Random;

import simulator.CA;

public class Hybridizer {
	private int _keepZone;
	private Random _randReproducers;
	private Random _randMutatorMask;
	private Random _randMutatorBase;
	public Hybridizer(byte keepZone){
		_keepZone = keepZone;
		_randReproducers = new Random(System.currentTimeMillis()+101);
		_randMutatorBase = new Random(System.currentTimeMillis()+141);
		_randMutatorMask = new Random(System.currentTimeMillis()+71);
	}
	public CA[] hybridize(CA[] population, byte childLimit, ParameterSet ps){
		Arrays.sort(population);
		int populationSize = removeWorst(population);
		if(populationSize==0){
			for(int index=0; index<population.length;index++){
				population[index]= new CA(ps.getStates());
			}
			return population;
		}
		int targetPopulation = population.length;
		int initialDefecit = targetPopulation - populationSize;
		int defecit = initialDefecit-1;
		while(defecit>=0){
			CA father = population[Math.abs((_randReproducers.nextInt())
			                          %populationSize)+initialDefecit];
			CA mother = population[Math.abs((_randReproducers.nextInt())
                    				  %populationSize)+initialDefecit];
			//System.out.println("YY"+mother);
			defecit = crossBreed(population, mother, father,childLimit,
					targetPopulation,defecit,ps);
		}
		return population;
	}
	public int removeWorst(CA[] CAs){
		int numberLeft = CAs.length - getNonZero(CAs);
		return getTopValues(CAs,numberLeft);
	}
	private int getTopValues(CA[] CAs, int numberLeft){
		if(numberLeft <= _keepZone){
			return numberLeft;
		}
		int numberToRemove = numberLeft-_keepZone;
		int start = CAs.length - numberLeft;
		for(int index = start; index< start +numberToRemove;index++){
			CAs[index]=null;
			numberLeft--;
		}
		return numberLeft;
	}
	private int getNonZero(CA[] CAs){
		int index=0;
		int removedCtr = 0;
		for(; index<CAs.length;index++){
			if(CAs[index].problemsSolved()==0){
				CAs[index]=null;
				removedCtr++;
			}
		}
		return removedCtr;
	}
	private int crossBreed(CA[] CAs,CA mother, CA father, 
			int offSpring, int targetPopulation, int defecit, ParameterSet ps){
		byte[] motherKey = mother.getKey();
		byte[] fatherKey = father.getKey();
		for(int index=0; index<offSpring && defecit >= 0;index++){
			CA child = new CA(ps.getStates());
			cross(child.getKey(), motherKey, fatherKey);
			mutate(child.getKey());
			//System.out.println("CC"+child.toString());
			CAs[defecit]=child;
			defecit--;
		}
		return defecit;
	}
	private void cross(byte[] childKey, byte[] motherKey, 
			byte[] fatherKey){
		for(int index=0; index<childKey.length; index++){
			childKey[index] = (byte) (motherKey[index]^fatherKey[index]);
		}
	}
	private void mutate(byte[] childKey){
		byte position = (byte)(_randMutatorMask.nextInt() % 3);
		byte base = (byte)(_randMutatorBase.nextInt()%2);
		byte mask = (byte)(base << position);
		for(byte index=0; index<position; index++){
			childKey[index] = (byte)(childKey[index] ^ mask);
			base = (byte)(_randMutatorBase.nextInt()%2);
			mask = (byte)(base << position);
			position = (byte)(_randMutatorMask.nextInt() % 3);
		}
	}
	public static void main(String[] args){
		Hybridizer h = new Hybridizer((byte)4);
		CA[] someCAs = new CA[9];
		CA ca1 = new CA((byte)2);
		CA ca2 = new CA((byte)2);
		CA ca3 = new CA((byte)2);
		CA ca4 = new CA((byte)2);
		ca4.solvesProblem();
		ca4.solvesProblem();
		ca4.solvesProblem();
		CA ca5 = new CA((byte)2);
		ca5.solvesProblem();
		ca5.solvesProblem();
		ca5.solvesProblem();
		ca5.solvesProblem();
		CA ca6 = new CA((byte)2);
		ca6.solvesProblem();
		ca6.solvesProblem();
		CA ca7 = new CA((byte)2);
		ca7.solvesProblem();
		ca7.solvesProblem();
		ca7.solvesProblem();
		ca7.solvesProblem();
		ca7.solvesProblem();
		CA ca8 = new CA((byte)2);
		ca8.solvesProblem();
		someCAs[0]=ca1;
		someCAs[1]=ca2;
		someCAs[2]=ca3;
		someCAs[3]=ca4;
		someCAs[4]=ca5;
		someCAs[5]=ca6;
		someCAs[6]=ca7;
		someCAs[7]=ca8;
		someCAs[8]=new CA((byte)2);
		CA[] nextGen= h.hybridize(someCAs,(byte)2, new ParameterSet());
		for(CA c : nextGen){
			if(c!=null){
				System.out.println(c);
			}
		}
	}
	
}

