package com.kc.tools;

public class ResetTimer  extends Timer{
double threshold;
public ResetTimer(double thresh) {
super();
setThreshold(thresh);
}
public void Update(long mi) {
super.Update(mi);

