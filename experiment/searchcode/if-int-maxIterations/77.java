package com.amd.aparapi.test;

public class ForIfMandel{
int width = 1024;

float scale = 1f;

int maxIterations = 10;

public void run() {
int tid = 0;

int i = tid % width;

