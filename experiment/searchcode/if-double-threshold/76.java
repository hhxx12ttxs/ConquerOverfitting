package org.godsboss.gaming.util;

public class RegularExecutor{
private final double threshold;
public RegularExecutor(Command command, double threshold){
this.command = command;
this.threshold = threshold;}

public void pass(double value){

