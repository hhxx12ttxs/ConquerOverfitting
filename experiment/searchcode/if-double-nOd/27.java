nod = nod.urm[0]; //am revenit pe nivelul 0 unde se face inserarea

if(nod == null || !nod.elem.cheie.equals(elem.cheie))
//	nod.elem = elem;	//daca cheia exista, ii actualizez valoarea;
nod = nod.urm[0];

if(nod != null &amp;&amp; nod.elem.cheie.equals(elem.cheie))	//daca nu a ajuns la final sau a gasit elem

