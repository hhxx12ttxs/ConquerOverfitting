mantisaResult = (~mantisaResult) + 1;
signoFinal = 1;
}

int expPos;
int expFinal;
System.out.println(&quot;Mantisa Resultado Pre correr: &quot; + mantisaResult);
if (expPos > 4) {

suffix = mantisaResult &amp; (int) (Math.pow(2, (expPos - 4)) - 1);

