package org.campus02.accounts;

public class SparKonto extends Konto{

public SparKonto(String inhaber) {
super(inhaber);
}

public void auszahlen (double wert)
{
if ( kontostand -wert <0 )

