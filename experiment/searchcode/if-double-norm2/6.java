public BigMandelbrotIterator(MathContext context, int maxIterationCount,
double maxNorm2) {
super(context, maxIterationCount, maxNorm2);
z = z.pow2(context).add(c, context);
if (z.pow2(context).norm2(context).compareTo(maxNorm2) > 0)
return i;
}

return -1;
}

}

