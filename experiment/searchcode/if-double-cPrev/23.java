public static AminoAcidModel getInstance(int modelID, double[] freq)
{
if (modelID == AminoAcidModelID.DAYHOFF)
else if (modelID == AminoAcidModelID.WAG)
{
return new WAG(freq);
}
else if (modelID == AminoAcidModelID.CPREV)

