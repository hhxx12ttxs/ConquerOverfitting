double kb = tam / 1024;
double mb = tam / 1048576;
double gb = tam / 1073741824;
DecimalFormat dec = new DecimalFormat(&quot;0.00&quot;);
tam_formateado = dec.format(mb).concat(&quot;MB&quot;);
}
if (gb > 0) {
tam_formateado = dec.format(gb).concat(&quot;GB&quot;);
}
return tam_formateado;
}
}

