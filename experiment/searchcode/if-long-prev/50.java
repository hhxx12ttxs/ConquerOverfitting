package org.fruct.oss.ikm;

public class Smoother {
private long prevTime = 0;
private float prevValue = 0;
public void insert(float value, long time) {
if (prevTime == 0) {
prevTime = time;
prevValue = value;

