private static ResizeHelper sharedHelper;
private double xFactor;
private double yFactor;

public static ResizeHelper getInstance(){
if(sharedHelper == null){
return sharedHelper;
}

protected ResizeHelper(){
this.setxFactor(1.0);
this.setyFactor(1.0);
}

public double getxFactor() {

