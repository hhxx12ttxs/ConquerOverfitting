// write back the fft data
if ( scale )
{
for ( int x = 0; x < complexSize-1; ++x )
{
cursorOut.getType().setComplexNumber( tempOut[ x * 2 ] / realSize, tempOut[ x * 2 + 1 ] / realSize );
cursorOut.fwd( 0 );
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
{
for ( int x = 0; x < complexSize-1; ++x )
{
// not enough memory
if ( complex == null )
return null;

