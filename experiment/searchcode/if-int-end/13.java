private int endInt;

public LCM(int startInt, int endInt) {
this.startInt = startInt;
this.endInt = endInt;
for (int i = startInt; i <= endInt; i++) {
if ( lcm % i != 0){
break;
}

if ( i == endInt){
return lcm;
}
}
lcm++;
}
}



}

