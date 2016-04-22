public class Solution {
    public String getPermutation(int n, int k) {
        if (n < 1) {
            return null;
        }
        
        int factorial = 1;
        for (int i = 2; i <= n; i++) {
            factorial *= i;
        }
        
        if (k > factorial) { 
            return null;
        }
        
        List<Integer> nums = new ArrayList<Integer>();
        for (int i = 1; i <= n; i++) {
            nums.add(i);
        }
        
        StringBuilder result = new StringBuilder();
        
        k--;
        
        for (int i = n; i >= 1; i--) {
            factorial /= i;
            
            int order = k / factorial;
            k = k % factorial;
            
            result.append(nums.get(order));
            nums.remove(order);
        }
        
        return result.toString();
    }
}

