package uk.ac.lkl.migen.system.ai.analysis.core.cbr;

import java.util.ArrayList;


/**
 * This class calculates all the possible permutations of 
 * an array of elements, returning them one by one. 
 * 
 * By returning the permutations one by one, this class
 * saves a lot of memory for array of big objects. As a 
 * rule of thumb, it is worth using IterativePermuter
 * when 
 * 
 * <ol>
 *  <li>sizeof(O) < sizeof(Integer), </li>
 *  <li>N*sizeof(O) << N^2*sizeof(O)</li>, and/or
 *  <li>N^2*sizeof(O) - N*sizeof(O) is a substantial saving of resources</li>
 * </ol>
 * 
 * This implementation can only handle lists of 12 elements max, 
 * because 13! > Integer.MAX_VALUE. My machine runs out of heap
 * memory with 10 elements anyway (Dell XPS 1330, RAM: 2GB). 
 * 
 * @author sergut
 *
 * @param <O> The type of the input array.
 */
public class IterativePermuter<O> {
    /**
     * The list of element to permute.
     */
    private ArrayList<O> listToPermute;
    
    /**
     * The index of the next permutation to be returned.
     * 
     * Starts at 0, ends at maxPermutations
     */
    private int counter;
    
    /**
     * The maximum number of permutations, that is, 
     * the square of the size of the list. 
     */
    private int maxPermutations;
    
    /**
     * A matrix with all the permutations of an array of 
     * integers as big as the list to be permuted. This 
     * list is consulted every time nextPermutation is called, 
     * and the corresponding elements of list are returned. 
     * 
     * Having permutedIndexes in memory uses less memory that
     * storing the whole set of permutations of the original 
     * list, by a factor sizeof(Integer)/sizeof(O) (if O is 
     * big enough so that N*sizeof(O) << N^2*sizeof(O)). 
     */
    ArrayList<ArrayList<Integer>> permutedIndexes; 
    
    /**
     * Create a new IterativePermuter for the given ArrayList.
     * 
     * @param list the list of elements to permute
     */
    public IterativePermuter(ArrayList<O> list) {
	this.listToPermute = list;
	int size = list.size();
	if (size > 12) 
	    throw new IllegalArgumentException("Max. 12 elements in list");
	    
	ArrayList<Integer> indexes = generateIndexes(size);
	BatchPermuter<Integer> permuter = new BatchPermuter<Integer>();
	permutedIndexes = permuter.permute(indexes);
	counter = 0;
	maxPermutations = factorial(size);
    }

    /**
     * Calculates the factorial of an integer.
     * 
     * @param i the integer
     * 
     * @return the factorial of the integer
     */
    private int factorial(int i) {
	if (i < 2) 
	    return 1;
	else 
	    return i * factorial(i-1); 
    }
    
    /** 
     * Generate an array with integers from 0 to size; in other
     * words, generate a list of indexes. 
     * 
     * @param size The size of the array to be returned.
     * 
     * @return an array with integers from 0 to size.
     */
    private ArrayList<Integer> generateIndexes(int size) {
	ArrayList<Integer> result = new ArrayList<Integer>();
	for (int i = 0; i < size; i++) {
	    result.add(i);
	}
	return result;
    }
     
    /**
     * Returns true when there are more permutations to 
     * be produced, false otherwise. 
     * 
     * @return true when there are more permutations, false otherwise.
     */
    public boolean hasMore() {
	if (counter < maxPermutations)
	    return true;
	else 
	    return false;
    }
    
    /**
     * Returns the next permutation.
     * 
     * @return the next permutation.
     * 
     * @throws RuntimeException when called and no more permutations
     * can be returned 
     */
    public ArrayList<O> nextPermutation() {
	if (!this.hasMore()) {
	    System.out.println("Ezxception!!!");
	    throw new RuntimeException("No more permutations");	    
	}
	
	ArrayList<O> result = new ArrayList<O>();
	ArrayList<Integer> indexes = permutedIndexes.get(counter);
	for (Integer index : indexes) {
	    O nextElement = listToPermute.get(index);
	    result.add(nextElement);
	}
	++counter;
	return result;
    }
    
    /** 
     * For testing, takes string arguments from the command line.
     * 
     * @param args several strings to be permuted.
     */
    public static void main(String args[]) {
	// Preparation
	if (args.length < 1) {
	    System.out.println("USAGE: ArrayPermuter <element> [... <element_n>]");
	    System.exit(0);
	}

	ArrayList<String> list = new ArrayList<String>();
	for (String element : args)
	    list.add(element);

	IterativePermuter<String> permuter = new IterativePermuter<String>(list);

	int counter = 0;
	while (permuter.hasMore()) {
	    ArrayList<String> nextIteration = permuter.nextPermutation();
	    System.out.println(nextIteration);
	    counter++;
	}
	System.out.println(counter + " permutations.");

    }
}

