/**
 * represents an complex number as BigDecimal 
 * some methods has presicion loss.
 */
public class Complex {
    public Complex() {
    }
    public Complex(BigDecimal real, BigDecimal img) {
        this.real = real;
    public Complex(double real, double img) {
        this.real = new BigDecimal(real);

