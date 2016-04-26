package com.blogspot.zelinskyi.core_java.generalJava;

/**
 * Created by IntelliJ IDEA.
 * User: dmytro
 * Date: Sep 9, 2011
 * Time: 9:46:28 PM
 *
 *  Overriding in object oriented programming, is a language feature that allows a subclass or child class to provide a
 *      specific implementation of a method that is already provided by one of its superclasses or parent classes.
 *
 * The rules for overriding a method are as follows:
 *  - The argument list must exactly match that of the overridden method.
 *  - The return type must be the same as, or a subtype of, the return type declared in the original overridden method in
 * the superclass.
 *  - The access level cannot be more restrictive than the overridden method's.
 *  - The access level CAN be less restrictive than that of the overridden method.
 *  - Instance methods can be overridden only if they are inherited by the subclass.
 *  - The overriding method CAN throw any unchecked (runtime) exception,regardless of whether the overridden method
 * declares the exception.
 *  Overridden methods may throw only the exceptions specified in their base-class versions, or exceptions derived
 * from the base-class exceptions.
 *  - You cannot override a method marked final.
 *  - You cannot override a method marked static.
 *  - If a method cannot be inherited, you cannot override it.
 *
 *
 *  Overloading
 *  Overloading method is feature of programming language that allows reuse the same method name in a class, but with
 *  different arguments.
 *  - Overloaded methods MUST change the argument list.
 *  - Overloaded methods CAN change the return type.
 *  - Overloaded methods CAN change the access modifier.
 *  - Overloaded methods CAN declare new or broader checked exceptions.
 *  - A method can be overloaded in the same class or in a subclass.
 *
 * */

// Example of overriding
class FirstExceprion extends Exception{}
class SecondException extends FirstExceprion{}

public class No_004 implements GeneralJavaQuestions{

    public static void main(String [] args) {}
}

class Vehicle{
    void ride(String s){}
    Object numberOfSeats(){return null;}
    protected String model() {return null;}
    int getSerialNumber(){return 0;}
    int madeIn()throws FirstExceprion{return 0;}
}

class Car extends Vehicle{
    //equal argument list
    void ride(String s){}
    //return type
    Integer numberOfSeats(){return 0;}
    //access layer
    public String model() {return null;}
    //throw unchecked exception
    int getSerialNumber(){
        throw new ArrayIndexOutOfBoundsException();
        }
    //throw checked exception
    int madeIn()throws SecondException{return 0;}
}

class Bike extends Vehicle{
    //equal argument list
    void ride(int speed){}
    //return type
    Object numberOfSeats(){return 0;}
    //access layer
    protected String model() {return null;}
    //throw unchecked exception
    int getSerialNumber() {
        throw new ArithmeticException();
    }
    //throw checked exception
    int madeIn()throws FirstExceprion{return 0;}
}

//Example of overloading

class Overload{

    public String m1(int var1, String var2){return null;}

    //Overloading
    public String m1(long var1, String var2){return null;}
    public String m1(int var1, long var2){return null;}
    public String m1(){return null;}

}

// Another good example of overloading

class Plant{}
class Tree extends Plant{}
class Garden{

    //1 ver
    boolean addPlane(Plant plants){return false;}
    //2 ver
    boolean addPlane(Tree tree){return false;}

    void checker() {
        Plant plant = new Plant();
        Tree tree = new Tree();
        Plant plantTree = new Tree();
        addPlane(plant);
        addPlane(tree);
        addPlane(plantTree); //will be using  1 ver method
    }
}



