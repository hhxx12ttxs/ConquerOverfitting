// not enough memory
if ( complex == null )
return null;
// write back the fft data
fft.realToComplex( -1, tempIn, tempOut );
if ( scale )
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
cursorOut.get().setComplexNumber( tempOut[ (complexSize-1) * 2 ] / realSize, tempOut[ (complexSize-1) * 2 + 1 ] / realSize );
}
{
final float[] tempOut = new float[ complexSize * 2 ];
}
// compute the fft in dimension 0 ( real -> complex )

