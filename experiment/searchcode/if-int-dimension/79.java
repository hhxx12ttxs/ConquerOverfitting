package de.invidit.ttt.entity;


public class Playfield extends GameState {

private int dimension;

public Playfield(int dimension) {
super(new String[dimension * dimension]);
this.dimension = dimension;

