package topcoder.srm583;

/**
 * Topcoder SRM 583
 * 
 * @author Oladeji Oluwasayo
 */
public class Problem1 {
    
    public String minNumber(String num) {
        char[] cArr = num.toCharArray();
        for (int a = 0; a < cArr.length; a++) {
            int minPos = min(cArr, a, cArr.length - 1);
            if (cArr[a] <= cArr[minPos])
                continue;
            else {
                swap(cArr, a, minPos);
                break;
            }
        }       
        return new String(cArr);
    }
    
    private void swap(char[] arr, int x, int y) {
        char z = arr[x];
        arr[x] = arr[y];
        arr[y] = z;
    }
    
    private int min(char[] arr, int x, int y) {
        int pos = x; char val = arr[x];
        
        for (int a = x+1; a <= y; a++) {
            if (arr[a] != '0' && arr[a] <= val) {
                val = arr[a]; pos = a;
            }
        }
        return pos;
    }
}

