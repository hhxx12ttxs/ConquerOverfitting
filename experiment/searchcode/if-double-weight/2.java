package de.bwvaachen.graph.logic;

public class Edge {

private double weight;

public Number getWeight() {
return weight;
}

public void setWeight(double weight) {
if ( weight < 0)
throw new IllegalArgumentException();
this.weight = weight;

