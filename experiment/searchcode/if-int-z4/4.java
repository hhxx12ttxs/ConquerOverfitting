// System.out.println(_p.getPrime(iX) + &quot;^2 + &quot; + _p.getPrime(iY) + &quot;^3 + &quot; + _p.getPrime(iZ) + &quot;^4 = &quot; + calcVal(iX, iY, iZ));
}
}
}
for (int i = 0; i < MAX_SUM; i++) {
if (baOn[i]) {
int iY3 = _p.getPrime(iY);
iY3 *= iY3 * iY3;
int iZ4 = _p.getPrime(iZ);
iZ4 *= iZ4 * iZ4 * iZ4;
return iX2 + iY3 + iZ4;
}
}

