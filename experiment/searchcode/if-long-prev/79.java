package com.ralitski.aether.input;

public class State {

private long prevTime;
private float prevState;

private StateCallback callback;

public void update(float state) {
if(state != prevState) {

