public class Factorial{  

    public static void main(String[] args) {  
        Factorial factorial=new Factorial();  
        int result=factorial.factorial_function(5);  
        System.out.println(result);  
    }  
    public int factorial_function(int index){  
        if(index==1){  
        return 1;  
        }else{  
        return factorial_function(index-1)*index;  
        }  
    }  
  
}  

