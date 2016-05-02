/**
 * Classe che modella i numeri Complessi e definisce le operazioni fondamentali come somma, 
 * prodotto, differenza, ecc.
 * @author Antonio
 *
 */
package it.sp4te.domain;

public class Complex {

	private double reale;
	private double immaginaria;

	public Complex(double reale, double immaginario){
		this.reale = reale;
		this.immaginaria = immaginario;
	}

	/**** GETTER & SETTER ****/
	
	public double getReale() {
		return reale;
	}

	public void setReale(double reale) {
		this.reale = reale;
	}

	public double getImmaginaria() {
		return immaginaria;
	}

	public void setImmaginaria(double immaginaria) {
		this.immaginaria = immaginaria;
	}

	public String toString(){
		String complex = "";

		if(this.immaginaria == 0)
			complex += this.reale;
		else if(this.immaginaria < 0 && this.reale!=0) 
			complex = this.reale +" "+ this.immaginaria+" j";
		else if(this.reale==0)
			complex = this.immaginaria+" j";
		else
			complex = this.reale+" + "+ this.immaginaria+" j";

		return complex;
	}
	
	/*************** METODI BASE NUMERI COMPLESSI ***************/
	
	/**
	 * Calcolo del modulo di un numero complesso visto come la radice quadrata
	 * del quadrato della parte reale + il quadrato della parte immaginaria
	 * @return double
	 */
	public double abs(){
		return Math.hypot(this.reale, this.immaginaria);
		
		//alternativa: Math.sqrt(Math.pow(this.reale,2) + Math.pow(this.immaginario,2));
	}
	
	public double fase() {
		return Math.atan2(this.immaginaria,this.reale);
	}
	
	public Complex coniugato(){
		return new Complex(this.reale, -this.immaginaria);
	}
	
	/**
	 * Effettua la somma tra il numero complesso su cui viene richiamato il metodo e il
	 * numero complesso passato da parametro
	 * @param b
	 * @return Complex
	 */
	public Complex somma(Complex b){
		Complex result = new Complex((this.reale+b.getReale()),
				(this.immaginaria+b.getImmaginaria()));
		return result;
	}
	
	public Complex differenza(Complex b){
		Complex result = new Complex((this.reale -  b.getReale()), 
				(this.immaginaria - b.getImmaginaria()));
		return result;
	}
	
	
	public Complex prodotto(Complex b){
		double reale = this.reale * b.getReale() - this.getImmaginaria() * b.getImmaginaria();
		double immag = this.immaginaria * b.getReale() + this.reale * b.getImmaginaria();
		return new Complex(reale, immag);
	}
	
	public Complex prodottoScalare(double s){
		return new Complex(this.reale*s, this.immaginaria*s);
	}
	
	
	public Complex reciproco(){
		double scalare = Math.pow(this.reale,2) + Math.pow(this.immaginaria,2);
		Complex result = new Complex(this.reale/scalare , - (this.immaginaria/scalare));
		return result;
	}
	
	public Complex rapporto(Complex b){
		return this.prodotto(b.reciproco());
	}
	
	public Complex pow(double n) {
		double reale = Math.pow(this.abs(), n) * Math.cos(n*this.fase());
		double immaginaria = Math.pow(this.abs(), n) * Math.sin	(n*this.fase());
		return new Complex(reale,immaginaria);
	}
	
	public Complex exp() {
		double reale = Math.exp(this.reale) * Math.cos(this.immaginaria);
		double immaginaria = Math.exp(this.reale) * Math.sin(this.immaginaria);
		return new Complex(reale,immaginaria);
	}
	
	public Complex log() {
		if (this.reale < 0)
			return null;
		double reale = Math.log(this.abs());
		double immaginaria = this.fase();
		return new Complex(reale, immaginaria);
	}
	
	public Complex sin() {
		double reale = Math.sin(this.reale)*Math.cosh(this.immaginaria);
		double immaginaria = Math.cos(this.reale)*Math.sinh(this.immaginaria);
		return new Complex(reale, immaginaria);
	}
	
	public Complex cos() {
		double reale = Math.cos(this.reale) * Math.cosh(this.immaginaria);
		double immaginaria = -Math.sin(this.reale) * Math.sinh(this.immaginaria);
	    return new Complex(reale,immaginaria);
	}
	
	public Complex tan() {
		return this.sin().rapporto(this.cos());
	}
	
	@Override
	public int hashCode() {
		return (int) (this.reale + this.immaginaria);
	}

	@Override
	public boolean equals(Object o) {

		Complex c = (Complex)o;

		if(this.getReale() == c.getReale() && this.getImmaginaria() == c.getImmaginaria())
			return true;

		else 
			return false;

	}	 
	
	//---- Copmletare la classe con ulteriori operazioni sui numeri complessi ----//
	
	
	public static void main(String[] args){
		
		//esempio di come creare due numeri complessi e applicare il metodo somma
		Complex a = new Complex(1,2);
		Complex b = new Complex(1, 1);
		
		Complex c = a.prodotto(b.coniugato());
		
		System.out.println(c.toString());
	}
}

