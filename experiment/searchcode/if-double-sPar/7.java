package Konto;

public class SparKonto extends Konto{

public SparKonto(String inhaber) {
super(inhaber);
}

public void auszahlen (double wert)
{
if ( kontostand -wert <0 )
{
System.out.println(&quot;Nicht genügend Guthaben&quot;);

