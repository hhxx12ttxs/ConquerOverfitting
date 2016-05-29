package com.ldm.model.geometry;

public class Vect {
public double dx;
public double dy;
public Vect normalize(){
double norm = Math.sqrt(dx * dx + dy * dy);
if(norm != 0){
this.dx /= norm;

