* Number of PTMs.
*/
private int nPTM;
/**
* The spectrum keys.
*/
private ArrayList<String> spectrumKeys;
for (int aa = 0; aa < peptide.getSequence().length(); aa++) {

int column = 1;
if (annotationPreferences.getFragmentIonTypes().contains(PeptideFragmentIon.A_ION)) {

