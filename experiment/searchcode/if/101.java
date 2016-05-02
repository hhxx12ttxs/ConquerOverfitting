public class If {
    public static void main(String[] arg) {
        int a = 5;
        int b = 10;

        if(a < b)
            a += 10;

        if(a > b) {
            a = 0;
            b = 20;
        }
    }
}

