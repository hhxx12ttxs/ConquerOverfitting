int a,b;
if(num<0)a=-num; else a=num;
if(den<0) b=-den; else b=-den;
int mcd=MCD(a,b);
num=num/mcd;
return fa;
}
public Frazione somma(Frazione fa){
Frazione fb=new Frazione();
int a,b;
if(den<0) a=-den; else a=den;

