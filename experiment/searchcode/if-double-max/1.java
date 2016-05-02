import gov.nasa.jpf.continuity.SymbolicRealVars;
import java.util.Map;

public class Max {
  public static double max(double x, double y) {
    if (x > y) {
      return x;
    } else {
      return y;
    }
  }
  public static double max2(double x, double y) {
    if (y < x) {
      return x;
    } else {
      return y;
    }
  }
  public static double max3(double x, double y) {
    if (x > y) {
      return x+1;
    } else if (x <= 10) {
      return y-2;
    } else {
      return x;
    }
  }
  public static double max4(double x, double y) {
    if (x >= y) {
      return x;
    } else  {
      return y;
    }
  }
  public static double max5(double x, double y) {
    if (y <= x) {
      return y;
    } else  {
      return x;
    }
  }


  public static void main(String[] args) {
    double xm = SymbolicRealVars.getSymbolicReal(-100.0, 100.0, "xm");
    double ym = SymbolicRealVars.getSymbolicReal(-100.0, 100.0, "ym");
    double mm = max5(xm, ym);
    System.out.println(mm);
    SymbolicRealVars.notePathFunction("mm");
    System.out.println(">>> PC PF map: ");
    for (Map.Entry<String,String> e :  SymbolicRealVars.getPcPfMap().entrySet()) {
      System.out.println("   PATH CONDITION: " + e.getKey());
      System.out.println("   PATH FUNCTION: " + e.getValue());
      System.out.println("--------------------------");
    }
  }
}
/*
public class Max extends java.lang.Object{
public Max();
  Code:
   0:   aload_0
   1:   invokespecial   #1; //Method java/lang/Object."<init>":()V
   4:   return

  public static double max(double x, double y) {
    if (x > y) {
      return x;
    } else {
      return y;
    }
  }
public static double max(double x, double y);
  Code:
   0:   dload_0    // x
   1:   dload_2    // y
   2:   dcmpl      //  --> 1 if x>y;  --> -1, 0 if x<=y
   3:   ifle    8  // THEN 1: goto 6 if x>y;  ELSE -1,0: goto 8 if x<=y
   6:   dload_0    // load x
   7:   dreturn    // return x
   8:   dload_2    // load y
   9:   dreturn    // return y

Path Conditions --> Path Functions
x > y --> x
x <= y --> y





  public static double max2(double x, double y) {
    if (y < x) {
      return x;
    } else {
      return y;
    }
  }
public static double max2(double, double);
  Code:
   0:	dload_2      // y
   1:	dload_0      // x
   2:	dcmpg        // -1 if y < x;   0 if y == x;   1 if y > x
   3:	ifge	8    // THEN -1: goto 6 if y < x;    ELSE 0,1: goto 8 if y >= x
   6:	dload_0      // y < x: THEN: x
   7:	dreturn
   8:	dload_2      // y >= x: ELSE: y
   9:	dreturn

Path Conditions --> Path Functions
y < x --> x
y >= x --> y



  public static double max3(double x, double y) {
    if (x > y) {
      return x+1;
    } else if (x <= 10) {
      return y-2;
    } else {
      return x;
    }
  }
public static double max3(double, double);
  Code:
   0:	dload_0     // x
   1:	dload_2     // y
   2:	dcmpl       // -1 if x < y;  0 if x = y;  1 if x > y
   3:	ifle	10  // Branch if x <= y;  Fall thru if x > y
   6:	dload_0     // x
   7:	dconst_1    // 1.0
   8:	dadd        // x + 1.0
   9:	dreturn     // return x + 1.0
   10:	dload_0     // x
   11:	ldc2_w	#2; // double 10.0d
   14:	dcmpg       // -1 if x < 10;  0 if x = 10;  1 if x > 10
   15:	ifgt	24  // Branch if x > 10;  Fall thru if x <= 10 
   18:	dload_2     // y
   19:	ldc2_w	#4; // double 2.0d
   22:	dsub        // y - 2
   23:	dreturn     // return y - 2
   24:	dload_0     // x
   25:	dreturn     // return x

Path Conditions & Functions:
x > y --> x + 1
x <= y && x <= 10 --> y - 2
x <= y && x > 10 --> x




  public static double max4(double x, double y) {
    if (x >= y) {
      return x;
    } else  {
      return y;
    }
  }
public static double max4(double, double);
  Code:
   0:	dload_0
   1:	dload_2
   2:	dcmpl
   3:	iflt	8
   6:	dload_0
   7:	dreturn
   8:	dload_2
   9:	dreturn
Path Conditions & Functions:
x >= y --> x
x < y  --> y


  public static double max5(double x, double y) {
    if (x <= y) {
      return y;
    } else  {
      return x;
    }
  }
public static double max5(double, double);
  Code:
   0:	dload_0     // x
   1:	dload_2     // y
   2:	dcmpg       // -1 if x < y;  0 if x = y;  1 if x > y
   3:	ifgt	8   // Branch if x > y;  Fall thru if x <= y
   6:	dload_2     // y
   7:	dreturn     // return y
   8:	dload_0     // x
   9:	dreturn     // return x
Path Conditions & Functions:
x <= y --> y
x > y  --> x
*/
