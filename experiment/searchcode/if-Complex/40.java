package electricUtils;

import mathUtils.Complex;
import mathUtils.MathUtils;

public class ElectricUtils {
public static Complex getEquivalentResistance(Complex resistance1, Complex resistance2,
boolean areAlongside) {
if (areAlongside) {
Complex numerator = MathUtils.ComplexUtils.multiplyComplex(resistance1, resistance2);

