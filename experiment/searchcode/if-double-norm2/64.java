package psiborg.fractal.generators;

import psiborg.fractal.MutableComplexDouble;

public class JuliaGenerator implements FractalGenerator {
public int steps(MutableComplexDouble z) {
int steps = 0;

while (steps < THRESHOLD_STEPS) {
if (z.norm2() > 4.0) {
return steps;

