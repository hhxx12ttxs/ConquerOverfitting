package com.aamend.dsa.structure;

import java.util.LinkedList;

public class Heap extends LinkedList<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(Integer e) {
		boolean success = super.add(e);
		minHeapify();
		return success;
	}

	/**
	 * 1. find the index of the value to delete 2. put the last value in the
	 * heap at the index location of the item to delete 3. verify heap ordering
	 * for each subtree which used to include the value
	 * 
	 * @param value
	 *            is the value to remove from the heap
	 * @return true if deleted, false otherwise
	 */
	public boolean delete(int value) {

		// Step 1
		// Find index of value to delete
		int idx = -1;
		for (int i = 0; i < this.size(); i++) {
			if (value == this.get(i)) {
				idx = i;
				break;
			}
		}

		if (idx < 0) {
			return false;
		}

		// Step 2
		// Update index with right most element
		int count = this.size() - 1;
		this.set(idx, this.get(count));
		this.remove(count);
		count--;

		// Step 3
		// Re-organize heap
		while (2 * idx + 1 < count
				&& (this.get(idx) > this.get(2 * idx + 1) || this.get(idx) > this
						.get(2 * idx + 2))) {

			int tmp = this.get(idx);

			// promote smallest key from subtree
			if (this.get(2 * idx + 1) < this.get(2 * idx + 2)) {
				this.set(idx, this.get(2 * idx + 1));
				this.set(2 * idx + 1, tmp);
				idx = 2 * idx + 1;
			} else {
				this.set(idx, this.get(2 * idx + 2));
				this.set(2 * idx + 2, tmp);
				idx = 2 * idx + 2;
			}

		}

		return true;
	}

	private void minHeapify() {
		int idx = this.size() - 1;
		// 1. (index - 1)/2 (parent index)
		// 2. 2 x index + 1 (left child)
		// 3. 2 x index + 2 (right child)
		while (idx > 0 && this.get(idx) < this.get((idx - 1) / 2)) {
			int parentIdx = (idx - 1) / 2;
			int tmp = this.get(idx);
			this.set(idx, this.get((idx - 1) / 2));
			this.set((idx - 1) / 2, tmp);
			idx = parentIdx;
		}
	}

	/**
	 * Sequential search
	 * @param value
	 * @return true if found
	 */
	public boolean contains(int value) {
		for(int i = 0; i < this.size(); i++){
			if(this.get(i) == value){
				return true;
			}
		}
		return false;
	}
		
	

}

