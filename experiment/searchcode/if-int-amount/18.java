package com.bilgin.accounting;

public class Amount {
private int value;

public Amount(int value) {
public boolean equals(Object o) {
if (this == o) return true;
if (!(o instanceof Amount)) return false;

Amount amount = (Amount) o;

