package polynomial;

public class Term{
private int degree;
private double coeff;

public Term(int degree, double coeff) {
this.degree = degree;
this.coeff = coeff;
}

public Term(Term term) {

