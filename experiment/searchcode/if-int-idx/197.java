package com.aamend.dsa.examples;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class PileOfDisks {

	public static final int NUMBER_OF_DISKS = 20;
	public static final int SMALLEST = 1;
	public static final int LARGEST = 20;

	public static Random rand = new Random();

	public static LinkedList<Integer> unsortedPile = new LinkedList<Integer>();
	public static LinkedList<Integer> sortedPile = new LinkedList<Integer>();
	public static LinkedList<Integer> priestsHeap = new LinkedList<Integer>();

	public static void main(String[] args) {
		PileOfDisks pile = new PileOfDisks();

		// I. Generate my data - unsorted pile
		System.out.println("This is my unsorted pile");
		System.out.println("*******************************");
		System.out.println("");
		pile.generateUnsortedPile();

		// II. Consume pile
		System.out.println("");
		System.out.println("This is my priest heap");
		System.out.println("*******************************");
		System.out.println("");
		pile.consumePile();

		// III. Retrieve max and store in sorted pile
		System.out.println("");
		System.out.println("This is my final sorted pile");
		System.out.println("*******************************");
		System.out.println("");
		while (!priestsHeap.isEmpty()) {
			int max = pile.retrieveMaxDiskFromHeap();
			sortedPile.add(max);
		}

		Iterator<Integer> it = sortedPile.descendingIterator();
		while (it.hasNext()) {
			System.out.println("|\t" + it.next() + "\t|");
		}
	}

	private void consumePile() {
		while (!unsortedPile.isEmpty()) {
			int disk = unsortedPile.poll();
			addDiskIntoPriestHeap(disk);
		}
		System.out.print("[ ");
		for (int disk : priestsHeap) {
			System.out.print(disk + " ");
		}
		System.out.println("]");
	}

	private void addDiskIntoPriestHeap(int disk) {
		priestsHeap.add(disk);
		int idx = priestsHeap.size() - 1;
		while (idx > 0) {
			int parentIdx = (idx - 1) / 2;
			if (priestsHeap.get(parentIdx) < priestsHeap.get(idx)) {
				swap(priestsHeap, parentIdx, idx);
				idx = parentIdx;
			} else {
				break;
			}
		}
	}

	private int retrieveMaxDiskFromHeap() {
		int max = priestsHeap.poll();
		reHeapify();
		
		return max;
	}

	private void reHeapify() {

		for (int idx = 0; idx < priestsHeap.size(); idx++) {

			int leftChild = 2 * idx + 1;
			int rightChild = 2 * idx + 2;
			int largest;

			if (leftChild < priestsHeap.size()
					&& priestsHeap.get(leftChild) > priestsHeap.get(idx)) {
				largest = leftChild;
			} else {
				largest = idx;
			}

			if (rightChild < priestsHeap.size()
					&& priestsHeap.get(rightChild) > priestsHeap.get(largest)) {
				largest = rightChild;
			}

			if (largest != idx) {
				swap(priestsHeap, largest, idx);
			}
		}

	}

	private void swap(LinkedList<Integer> list, int i, int j) {
		int tmp = list.get(i);
		list.set(i, list.get(j));
		list.set(j, tmp);
	}

	private void generateUnsortedPile() {
		for (int i = 0; i < NUMBER_OF_DISKS; i++) {
			int rand = randomInt();
			unsortedPile.add(rand);
			System.out.println("|\t" + rand + "\t|");
		}
	}

	private static int randomInt() {
		return SMALLEST + rand.nextInt(LARGEST + 1 - SMALLEST);
	}

}

