* coeff[1] -> 0.05
* coeff[2] -> 0.1
* coeff[3] -> 0.15
* coeff[4] -> -1000
*/
private double[] coeff = {0, 0.05, 0.1, 0.15, -1000};
// ritorno il coeff corretto corrispondente al numero di pezzi cattivi mangiati
if (giocatore == 1)
return coeff[(4 - tavolo.vettorePezzi((byte) 4).size())];

