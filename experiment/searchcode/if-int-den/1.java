if (den > F.den) {
int commonD = den / F.den;
num = F.num*commonD + num;
}
if (den < F.den) {
int commonD = F.den / den;
public void simplify(){
if (den / num != 0){
int common = den / num;
den = den / num;
num = num / common;
}
}

}

