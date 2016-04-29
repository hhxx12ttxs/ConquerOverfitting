package package1;

public class ComplexNumber 
{
    
    // VARIABLES
    
    private int r;
    private int i;
    
    // CONSTRUCTORES
    
    public ComplexNumber()
    {
        this.r=1;
        this.i=0;
    }
    
    public ComplexNumber (int x, int y)
    {
        this.r=x;
        this.i=y;
    }
    
    public ComplexNumber(ComplexNumber cnumb)
    {
        this(cnumb.r, cnumb.i); 
    }
    
    // MÉTODOS
    
    public int getR ()
    {
        return this.r;
    }
    
    public int getI ()
    {
        return this.i;
    }
    
    public void setR (int x)
    {
        this.r=x;
    }
    
    public void setI (int x)
    {
        this.i=x;
    }
    
    public void setComplex (int x, int y)
    {
        setR(x);
        setI(y);
    }
    
    public void setComplex (ComplexNumber cnumb)
    {
        setComplex(cnumb.r, cnumb.i);
    }
    
    public String toString ()
    {
        return "El numero complejo es "+this.r+","+this.i;
    }
    
    public boolean equals (ComplexNumber cnumb)
    {
        return (equals(cnumb.r, cnumb.i));
    }
    
    public boolean equals (int x, int y)
    {
        return (this.r==x && this.i==y);
    }
    /* return (this.r==x && this.i==y) es igual que el procedimient if (tal==tal)
     * then return true/false etc... */
    
    public ComplexNumber addComplex (ComplexNumber cnumb)
    {
        return addComplex(cnumb.r, cnumb.i);
    }
    
    public ComplexNumber addComplex (int x, int y)
    {
        return new ComplexNumber(this.r+x, this.i+y);
    }
    
    public ComplexNumber lessComplex (int x, int y)
    {
        return new ComplexNumber (this.r-x, this.i-y);
    }
    
    public ComplexNumber lessComplex (ComplexNumber cnumb)
    {
        return lessComplex (cnumb.r, cnumb.i);
    }
    
    public ComplexNumber multiplyComplex (int x, int y)
    {
        return new ComplexNumber ((this.r*x-this.i*y),(this.r*y+this.i*x));
    }
    
    public ComplexNumber multiplyComplex (ComplexNumber cnumb)
    {
        return multiplyComplex (cnumb.r, cnumb.i);
    }
    
    public ComplexNumber divideComplex (int x, int y)
    {
        return new ComplexNumber (((int) ((this.r*x+this.i*y)/(Math.pow(x,2)+Math.pow(y,2)))), ((int) ((this.i*x-this.r*y)/(Math.pow(x,2)+Math.pow(y,2)))));
    }
    
    public ComplexNumber divideComplex (ComplexNumber cnumb)
    {
        return divideComplex (cnumb.r,cnumb.i);
    }        
}
