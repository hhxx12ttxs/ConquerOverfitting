package examen2013;

public class PolynomeArray implements Polynome{

private double coeff[];

public PolynomeArray(int maxDegree){
coeff = new double[maxDegree];
for(int i = 0; i < coeff.length; ++i){

