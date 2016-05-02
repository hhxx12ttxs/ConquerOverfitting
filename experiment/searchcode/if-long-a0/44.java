import java.sql.Timestamp;
import java.util.Date;


public class HeapSort {

	private static long[] a;
    private static int n;
    private static int left;
    private static int right;
    private static int largest;
    static int iter=0;

    
    public static void buildheap(long []a){
        n=a.length-1;
        for(int i=n/2;i>=0;i--){
            maxheap(a,i);
        }
    }
    
    public static void maxheap(long[] a2, int i){ 
        left=2*i;
        right=2*i+1;
        if(left <= n && a2[left] > a2[i]){
            largest=left;
        }
        else{
            largest=i;
        }
        
        if(right <= n && a2[right] > a2[largest]){
            largest=right;
        }
        if(largest!=i){
            exchange(i,largest);
            maxheap(a2, largest);
            iter++;
        }
    }
    
    public static void exchange(int i, int j){
        int t=(int) a[i];
        a[i]=a[j];
        a[j]=t; 
        }
    
    public static void sort(long[] a0){
    	Date czas = new Date();
		Timestamp date = new Timestamp(czas.getTime());
		System.out.println(iter +" "+date);
    	
    	a=a0;
        buildheap(a);
        
        for(int i=n;i>0;i--){
            exchange(0, i);
            n=n-1;
            maxheap(a, 0);
        }
        Date czas2 = new Date();
		Timestamp date2 = new Timestamp(czas2.getTime());
		System.out.println(iter+" "+date2);
		iter=0;
    }
}

