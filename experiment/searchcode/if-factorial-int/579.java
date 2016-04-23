

package factorial;

/**
 *
 * @author keimiokamoto
 */
public class Factorial {
    
    int result;
    
    public int factorial (int num){
        
        if (num==0){
            return result;
        }else{
            result = num * (num-1);
            
        }
        return factorial(num-1);
    }
    
    
    public static void main(String[] args) {
        
        Factorial fac = new Factorial();
     
        int answer = fac.factorial(3);
        System.out.println(answer);
        
    }
    
}

