package math;

public class Vector3D {
public double x, y, z;
public Vector3D(){
x = y = z = 0;
}
public Vector3D(Vector3D v){
this.x = v.x;
this.y = v.y;
this.z = v.z;

}
public Vector3D(double x, double y, double z){

