package edu.gac.mcs178.gack.domain;

public class Gold extends Thing {
private int amount;
public void addAmount(int amount) { this.amount += amount; }
public void subAmount(int amount) { if (this.amount < amount) this.amount = 0; else this.amount -= amount; }

