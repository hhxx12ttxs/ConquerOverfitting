spectrumAnnotator = new PeptideSpectrumAnnotator();
}

int nPTM = 0;
if (peptide.isModified()) {
for (ModificationMatch modMatch : peptide.getModificationMatches()) {
nPTM++;
}
}
}
}
}
if (nPTM == 0) {

