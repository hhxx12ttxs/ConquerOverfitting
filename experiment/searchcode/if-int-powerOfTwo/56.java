package org.chenchun;

public class PowerofTwo {
public boolean isPowerOfTwo(int n) {
int i = 0;
while (i < 32) {
int v = 1 << i;
if (v == n) {

