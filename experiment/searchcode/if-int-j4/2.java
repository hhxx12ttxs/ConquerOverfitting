{  ZERO = 0.0D; } //PARAMETER          ( ZERO = 0.0D0 )
//*     ..
//*     .. Local Scalars ..
int J4, J4P2; //INTEGER            J4, J4P2
DblRef.set(pDN, DN, pDNM1, DNM1, pDNM2, DNM2);
return; //IF( ( N0-I0-1 ).LE.0 )RETURN
}
//*
J4 = 4*I0 + PP - 3 - 1; //todo: note -1; //J4 = 4*I0 + PP - 3

