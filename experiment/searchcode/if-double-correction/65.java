public Boolean process() throws Exception {
if (! efficiencyCorrection_enable || efficiencyCorrection_mapURI == null){
return efficiencyCorrection_stop;
}
if (isEfficiencyFileChanged || efficiencyMap == null){
IGroup efficiencyData = null;

