package library;

import java.io.Serializable;


public class Frac extends Object implements Serializable{
	
	private static final long serialVersionUID = 2702201204191L;
	
	private int num, den;
	
    public Frac(int num){ this(num,1); }
	public Frac(int num, int den){
		this.num = num;
		this.den = den;
		if(this.den<0){
			this.num *= -1;
			this.den *= -1;
		}
		this.cancel();
	}
	public Frac(double in){
		int e = Double.toString(in-(int)in).length()-2;
		this.den = (int)Math.pow(10,e);
		this.num = (int)(in*this.den);
		if(this.den<0){
			this.num *= -1;
			this.den *= -1;
		}
		this.cancel();
	}
	public Frac(float in){
		this(1d*in);
	}
	
	public String toString(){ return this.num+"/"+this.den; }
	public Frac copy(){ return new Frac(this.num , this.den); }
	
	public int getNum(){ return this.num; }
	public int getDen(){ return this.den; }
	public double toDouble(){ return (double)this.num/this.den; }
	
	public Frac add(Frac f){
		return new Frac(this.num*f.den+f.num*this.den , this.den*f.den).cancel();
	}
	public Frac add(int i){
		return new Frac(this.num+i*this.den , this.den).cancel();
	}
	public Frac add_inp(Frac f){
		this.num = this.num*f.den+f.num*this.den;
		this.den *= f.den;
		return this.cancel();
	}
	public Frac add_inp(int i){
		this.num += i*this.den;
		return this.cancel();
	}
	
	public Frac sub(Frac f){
		return new Frac(this.num*f.den-f.num*this.den , this.den*f.den).cancel();
	}
	public Frac sub(int i){
		return new Frac(this.num-i*this.den , this.den).cancel();
	}
	public Frac sub_inp(Frac f){
		this.num = this.num*f.den-f.num*this.den;
		this.den *= f.den;
		return this.cancel();
	}
	public Frac sub_inp(int i){
		this.num -= i*this.den;
		return this.cancel();
	}
	
	public Frac mul(Frac f){
		return new Frac(this.num*f.num , this.den*f.den).cancel();
	}
	public Frac mul(int i){
		return new Frac(i*this.num , this.den).cancel();
	}
	public Frac mul_inp(Frac f){
		this.num *= f.num;
		this.den *= f.den;
		return this.cancel();
	}
	public Frac mul_inp(int i){
		this.num *= i;
		return this.cancel();
	}
	
	public Frac div(Frac f){
		return new Frac(this.num*f.den , this.den*f.num).cancel();
	}
	public Frac div(int i){
		return new Frac(1 , i).mul_inp(this).cancel();
	}
	public Frac div_inp(Frac f){
		this.num *= f.den;
		this.den *= f.num;
		return this.cancel();
	}
	public Frac div_inp(int i){
		this.den *= i;
		return this.cancel();
	}
	
	public Frac inverse(){
		return new Frac(this.den, this.num);
	}	
	public Frac inverse_inp(){
		int t = this.num;
		this.num = this.den;
		this.den = t;
		return this;
	}
	
	public Frac mod(Frac f){
		Frac ret = this.sub(f.mul((int)this.div(f).toDouble())); 
		if(this.num<0) ret = f.add(ret);
		return ret.cancel(); 
	}
	public Frac mod(int i){
		return new Frac(this.num , this.den).mod(new Frac(i));
	}
	public Frac mod_inp(Frac f){
		this.sub_inp(f.mul((int)this.div(f).toDouble())); 
		if(this.num<0) this.add_inp(f);
		return this.cancel(); 
	}
	public Frac mod_inp(int i){
		return this.mod(new Frac(i));
	}
	
	public Frac abs(){
		return new Frac(Math.abs(this.num) , Math.abs(this.den));
	}
	public Frac abs_inp(){
		if(this.num<0) this.num *= -1;
		if(this.den<0) this.den *= -1;
		return this;
	}
	
	public boolean equals(Frac f){ if(this.den==f.den && this.num==f.num) return true; return false; }
	public boolean equals(int i){ return this.equals(new Frac(i)); }
	public boolean lessThan(Frac f){ if(this.toDouble()<f.toDouble()) return true; return false; }
	public boolean lessThan(int i){ return this.lessThan(new Frac(i)); }
	public boolean greaterThan(Frac f){ if(this.toDouble()>f.toDouble()) return true; return false; }
	public boolean greaterThan(int i){ return this.greaterThan(new Frac(i)); }
	public boolean lessThanEqual(Frac f){  if(this.lessThan(f) || this.equals(f)) return true; return false; }
	public boolean lessThanEqual(int i){ return this.lessThanEqual(new Frac(i)); }
	public boolean greaterThanEqual(Frac f){  if(this.greaterThan(f) || this.equals(f)) return true; return false; }
	public boolean greaterThanEqual(int i){ return this.greaterThanEqual(new Frac(i)); }
	
	private Frac cancel(){
		if(this.num==0){
			this.num = 0;
			this.den = 1;
		}
		else if(this.den==0){
			this.num = 0;
			this.den = 0;
		}
		else{
			int ggT = ggT(this.num, this.den);
			this.num /= ggT;
			this.den /= ggT;
		}
		return this;
	}
	
    private int ggT(int a, int b){ 
    	a = Math.abs(a); b = Math.abs(b);
		int k = 0, t;
		while(a%2==0 && b%2==0){ a /= 2; b /= 2; k++; }
		if(a%2==1) t = -b;
		else t = a;
		while(t!=0){
			while(t%2==0) t /= 2;
			if(t>0) a = t;
			else b = -t;
			t = a-b;
		}
		return a*(int)Math.pow(2,k);
    }
}

