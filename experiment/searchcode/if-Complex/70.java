/*
Shady Salaheldin
*/

public class Complex {

    

    public Complex(double real, double img) {

        this.real = real;
        this.img = img;
    }

    public Complex add(Complex o) {

        return new Complex(real + o.real, img + o.img);
    }

    public Complex add(double n) {

        return new Complex(real + n, img);
    }

    public Complex div(Complex o) {

        return new Complex(((real * o.real) - (img * -1 *o.img)) / ((o.real * o.real) +(o.img * o.img)) , (real * ( -1 * o.img) + (img * o.real))/((o.real * o.real) +(o.img * o.img)));
    }

    public Complex div(double n) {

        return new Complex(real / n, img / n);
    }

    public Complex mul(Complex o) {

        return new Complex(real * o.real - img * o.img, real * o.img + img * o.real);
    }

    public Complex mul(double n) {

        return new Complex(real * n, img * n);
    }

    public Complex sub(Complex o) {

        return new Complex(real - o.real , img - o.img);
    }

    public Complex sub(double n) {

        return new Complex(real - n, img);
    }

    public String toString() {

	if ( real == 0){
		if(img == 0){
				  return "0";   
		 }
		 else{
				  return img + "i"; 
		 }
		 
		}
    
         
    if ( img == 0){
         return ""+real;
    } 
         
    if (img < 0){
       double myimg = -1 * img;       
	   return "(" + real + " - " + myimg + "i" + ")";
    }
    else{
         return "(" + real + " + " + img + "i" + ")";
    }  
	
    }

    private double real;
    private double img;
}
