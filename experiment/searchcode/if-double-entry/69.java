package jhn.counts.d;

import java.util.Map.Entry;

import jhn.util.RandUtil;


public abstract class AbstractDoubleCounter<K> implements DoubleCounter<K> {
double sum = 0.0;

for(Entry<K, Double> entry : this.entries()) {
sum += entry.getValue().doubleValue();
if(sum >= pos) {

