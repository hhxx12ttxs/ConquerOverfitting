package src.util.pilas;

import src.util.Nods.DoubleNod;

public class NodPila<T> implements Pila<T> {
public void create() {
first=null;

}



public void push(T e) {
DoubleNod<T> nuevo=new DoubleNod<T>(e);

if(isEmpty()){

