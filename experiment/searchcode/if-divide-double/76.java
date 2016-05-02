/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author pashathebeast
 */
public class MVector {
    private double x;          // x - coordinate
    private double y;          // y - coordinate
    
    public double getX()                               { return x; }
    public void setX(double x)                         { this.x = x; }
    
    public double getY()                               { return y; }
    public void setY(double y)                         { this.y = y; }
    
    public double getLength()                          { return Math.sqrt(x*x + y*y); }
    
    public MVector(){
        this.x = 0;
        this.y = 0;
    }
    
    public MVector(double _x, double _y){
        this.x = _x;
        this.y = _y;
    }
    
    public MVector(double angle){
        this.x = Math.cos(angle);
        this.y = Math.sin(angle);
    }
    
    
    public void Add(MVector v){
        this.x += v.getX();
        this.y += v.getY();
    }
    
    public void Subtract(MVector v){
        this.x -= v.getX();
        this.y -= v.getY();
    }
    
    public void Multiply(double value){
        this.x *= value;
        this.y *= value;
    }

    public void Divide(double value){
        this.x /= value;
        this.y /= value;
    }
    
    public static MVector Add(MVector one, MVector another){
        return new MVector(one.getX() + another.getX(), one.getY() + another.getY());
    }
    
    public static MVector Subtract(MVector one, MVector another){
        return new MVector(one.getX() - another.getX(), one.getY() - another.getY());
    }
    
    public static MVector Multiply(MVector one, double value){
        return new MVector(one.getX() * value, one.getY() * value);
    }
    
    public static MVector Divide(MVector one, double value){
        return new MVector(one.getX() / value, one.getY() / value);
    }
    
    public static MVector Normalize(MVector one){
        return new MVector(one.getX()/one.getLength(),one.getY()/one.getLength() );
    }
    
    public static double Distance(MVector one, MVector two) {
        return Math.sqrt(Math.pow((one.x - two.x), 2) + Math.pow((one.y - two.y), 2));
    }
    
    public void Normalize(){
        this.Divide(this.getLength());
    }
    
    public void Limit(double value){
        if (this.getLength() > value) {
            this.Normalize();
            this.Multiply(value);
        }
    }
}

