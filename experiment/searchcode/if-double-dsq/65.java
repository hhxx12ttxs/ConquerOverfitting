* @param dsq size of box squared
*/
protected <region R1>void walksub(Node p, double dsq, double tolsq, HGStruct<R1> hg,
if (r != null)
walksub(r, dsq / 4.0, tolsq, hg, level+1);
}
}
else if (p != (Node) hg.pskip)   {

