package tri12;

public class p2 {
private static p2 sole;

private p2(){}

public static p2 create(){
if(sole!=null)
sole = new p2();
return sole;
}
}

