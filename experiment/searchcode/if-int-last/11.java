/**
*             Copyright 2013 SenselessSolutions
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package org.senselesssolutions.utils.pagination;

import java.util.Arrays;

/**
 * @author Sergio Aparicio Escribano
 *
 */
public class Pagination {
	
	private int first;
	private int previous;
	private int next;
	private int last;
	private int[] pages;
	
	public Pagination(int currentPage, int totalItems, int pageSize, int navLength){
		this.first = 0;
		this.last = (int)(Math.ceil((double)totalItems/(double)pageSize)) - 1;
		this.previous = Math.max(0, currentPage-1);
		this.next = Math.min(currentPage+1, this.last);
		this.pages = calculatePages(currentPage, this.last, navLength);
	}
	
	private int[] calculatePages(int currentPage, int lastPage, int navLength){
		int[] pages = new int[Math.min(navLength, lastPage+1)];
		int middle = 0;
		int pivot = (int)Math.floor((double)navLength/(double)2);
		
		if(currentPage - pivot >= 0 && currentPage + pivot <= lastPage){
			middle = currentPage;
		}
		else if(currentPage - pivot < 0){
			middle = pivot;
		}
		else {//currentPage + pivot > lastPage
			middle = lastPage - pivot;
		}
		
		int startPage = middle - pivot;
		for(int i = 0; i < pages.length; i++){
			pages[i] = startPage + i;
		}
		
		return pages;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getPrevious() {
		return previous;
	}

	public void setPrevious(int previous) {
		this.previous = previous;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public int getLast() {
		return last;
	}

	public void setLast(int last) {
		this.last = last;
	}

	public int[] getPages() {
		return pages;
	}

	public void setPages(int[] pages) {
		this.pages = pages;
	}

	@Override
	public String toString() {
		return "Pagination [first=" + first + ", previous=" + previous
				+ ", next=" + next + ", last=" + last + ", pages="
				+ Arrays.toString(pages) + "]";
	}

}

