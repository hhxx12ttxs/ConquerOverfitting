String peptideSequence = peptide.getSequence();
for (int site : scores.keySet()) {
if (site == 0) {
nConfident = ptmConfidentSites.size();
}
if (nConfident < nPtm) {
int nRepresentatives = nPtm - nConfident;

