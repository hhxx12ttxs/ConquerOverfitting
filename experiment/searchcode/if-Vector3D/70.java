package com.pi.math;

public class Vector3D {
public float x, y, z;

public Vector3D(float x, float y, float z) {
return (int) x << 24 ^ (int) y << 12 ^ (int) z << 6;
}

@Override
public boolean equals(Object o) {
if (o instanceof Vector3D) {

