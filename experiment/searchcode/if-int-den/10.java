import java.util.Arrays;

class Ratio implements Comparable{
    protected int num;
    protected int den;
    
    private Ratio(){}

    public Ratio(int num, int den){
        this.num = num;
        this.den = den;
    }
    public int compareTo(Object object){
        if(this == object){
            return 0;
        }

        if(!(object instanceof Ratio)){
            throw new IllegalArgumentException("Ratio expected");
        }

        Ratio that = (Ratio)object;
        return (this.num * that.den - that.num * this.den);
    }

    public String toString() {
        return String.format("%d/%d", num, den);
    }
    
    public double value(){
        return ((double)num) / den;
    }
}

public class TestSort{
    public static void main(String[] args){
        String[] data = {"haha", "nice", "helloworld", "How are you"};
        System.out.print("Original data:");
        System.out.print(Arrays.asList(data));

        // Arrays.sort(data);
        // System.out.print("Sorting result:");
        // print(data);


        // // test on ratio
        // Ratio[] ratios = new Ratio[3];
        // ratios[0] = new Ratio(22, 7);
        // ratios[1] = new Ratio(25, 8);
        // ratios[2] = new Ratio(28, 9);

        // Arrays.sort(ratios);
        // print(ratios);

    }

    // private static <T> void print(T[] array){
    //     for(T elem : array){
    //         System.out.printf("%s ", elem);
    //     }
    //     System.out.println();
    // }
}
