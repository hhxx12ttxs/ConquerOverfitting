Double primero = Double.parseDouble(x);
Double segundo = Double.parseDouble(y);
if (segundo==0){
throw new DivisorCero(&quot;ERROR&quot;);
}
Double resultadoDividir = primero / segundo;
return resultadoDividir;
}
}

