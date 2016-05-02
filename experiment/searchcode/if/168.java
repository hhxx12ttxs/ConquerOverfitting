public class app {
public static void main(String[] args) {
for(int i = 1; i<=10; i++){
if (i<2) {
System.out.print("Less than two");
}
if (i>5)
{
System.out.print("Greater than five");
}
else {
System.out.print("Neither less than two, nor greater than five");
}
}
}



	public static int[] array(int... values){
		return values;
	}
	public static String[] array(String... values){
		return values;
	}
	public static boolean[] array(boolean... values){
		return values;
	}

}
