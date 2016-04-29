package electricUtils;

import mathUtils.Complex;
import mathUtils.MathUtils;

public class ElectricUtils {
	public static Complex getEquivalentResistance(Complex resistance1, Complex resistance2, 
			boolean areAlongside) {
		if (areAlongside) {
			Complex numerator = MathUtils.ComplexUtils.multiplyComplex(resistance1, resistance2);
			Complex denominator = MathUtils.ComplexUtils.sumComplex(resistance1, resistance2);
			
			return MathUtils.ComplexUtils.divideComplex(numerator, denominator);
		} else {
			return MathUtils.ComplexUtils.sumComplex(resistance1, resistance2);
		}
	}
	
	public static Complex getElectricityByTevenen(Complex voltage, Complex equivalentResistance, 
			Complex currentResistance) {
		Complex denominator = MathUtils.ComplexUtils.sumComplex(equivalentResistance, currentResistance);
		
		return MathUtils.ComplexUtils.divideComplex(voltage, denominator);
	}
	
	public static Complex getElectricityByNorton(Complex shortCircuitElectricity, Complex equivalentResistance, 
			Complex currentResistance) {
		Complex numerator = MathUtils.ComplexUtils.multiplyComplex(shortCircuitElectricity, equivalentResistance);
		Complex denominator = MathUtils.ComplexUtils.sumComplex(equivalentResistance, currentResistance);
		
		return MathUtils.ComplexUtils.divideComplex(numerator, denominator);
	}
	
	public static Complex getVoltageFromElectricityAndResistance(Complex electricity, Complex resistance) {
		return MathUtils.ComplexUtils.multiplyComplex(electricity, resistance);
	}
	
	public static Complex getResistanceFromElectricityVoltageAndPower(
			Complex electricity, Complex voltage, Complex power) {
		double angle = Math.acos(
				power.getModule() / 
				(electricity.getModule() * voltage.getModule()));
		double cosineValue = Math.cos(angle);
		double sineValue = Math.sin(angle);
		double resistance = MathUtils.ComplexUtils.divideComplex(voltage, electricity).getModule();
		
		return new Complex(resistance * cosineValue, resistance * sineValue);
	}
	
	public static Complex getPowerFromVoltageAndElectricity(Complex voltage, Complex electricity) {
		return MathUtils.ComplexUtils.multiplyComplex(voltage, electricity.getComplexConjugate());
	}
}

