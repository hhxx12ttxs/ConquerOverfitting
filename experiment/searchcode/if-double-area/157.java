package Numero17;

import java.lang.Math;

public class Triangulo {

	private double base;
	private double lado1;
	private double lado2;
	
	public Triangulo(double base, double lado1, double lado2) {
		this.base = base;
		this.lado1 = lado1;
		this.lado2 = lado2;
	}
	
	public boolean aprovaTriangulo(){
		if ((this.base > (this.lado1 + this.lado2)) || (this.lado1 > (this.base + this.lado2))
				|| (this.lado2 > (this.base + this.lado1)))
			return false;
		return true;
	}

	public String classificaTriangulo() {
		if (aprovaTriangulo()){
			if ((this.base == this.lado1) && (this.base == this.lado2))
				return "Triangulo Equilatero";
			else if ((this.base == this.lado1) || (this.base == this.lado2) || (this.lado1 == this.lado2))
				return "Triangulo Isosceles";
			return "Triangulo Escaleno";
		}
		return "Os valores infomados nao formam um triangulo.";
	}
	
	public double areaTrianguloEquilatero(){
		double area = (this.lado1*this.lado2*Math.sqrt(3))/4;
		return area;
	}
	
	public double areaTrianguloIsosceles(){
		double hipotenusa = Math.pow((this.base/2), 2) + Math.pow(this.lado1, 2);
		double area = (this.base * hipotenusa)/2;
		return area;
	}
	
	public double areaTrianguloEscaleno(){
		double p = (this.base + this.lado1 + this.lado2)/2;
		double area = Math.sqrt(p * (p-this.base) * (p-this.lado1) * (p-this.lado2));
		return area;
	}
}

