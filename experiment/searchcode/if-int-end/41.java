package SortingAlgorithms;

public class FlipSort implements SortAlgorithm {

	
	/** FLIPSORT 
	 *
	 */

	public int[] flip(int[] r, int start, int end){

	    int i = start;
	    int k = end;
	    
	    while(i<=k){
	    
	        wissel(r,i,k);
	        
	        i++;
	        k--;
	        
	    }
	    
	    return r;

	}



	public int[] flipsort(int[] r){

	    int end = r.length;
	    
	    
	    while(end!=1){
	        int maxpos=0;
	    
	        for(int i=0;i<end;i++){
	            if(r[i]>r[maxpos]){
	                maxpos=i;
	            }
	        }
	        flip(r,0,maxpos);
	        flip(r,0,end-1);
	        end--;
	        
	    }
	    
	    return r;

	}
	
	public void wissel(int[] r, int i, int j){
		int temp = r[i];
		r[i]=r[j];
		r[j]=temp;
	}



	@Override
	public void run(int[] array) {
		// TODO Auto-generated method stub
		flipsort(array);
	}
}

