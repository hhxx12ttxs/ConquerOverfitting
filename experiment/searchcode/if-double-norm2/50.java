package psiborg.fractal.generators;

import psiborg.fractal.MutableComplexDouble;

public class CubicMandelbrotGenerator implements FractalGenerator {
MutableComplexDouble z = new MutableComplexDouble(c);
MutableComplexDouble t = new MutableComplexDouble(z);

while (steps < THRESHOLD_STEPS) {
if (z.norm2() > 16.0) {

