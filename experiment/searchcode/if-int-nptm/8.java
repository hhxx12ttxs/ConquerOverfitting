for (double ptmMass : modificationMasses) {

int nPtm = peptide.getNVariableModifications(ptmMass);
if (nConfident < nPtm) {
int nRepresentatives = nPtm - nConfident;
if (nRepresentatives > 0) {

