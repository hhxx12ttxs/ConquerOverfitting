Double f11 = (Math.pow(a,2) + Math.pow(b,2))/((Math.pow(a,2) - Math.pow(b,2)));
Double pow = (a + b + c) / Math.sqrt(c);
Double resultF1= Math.pow(f11,pow);
Double f2 = Math.pow(a,2) + Math.pow(b,2) - Math.pow(c,3);
Double averageF12 = (resultF1 + resultF2)/2;
Double differance = averageAbc - averageF12;
if (differance < 0) {

