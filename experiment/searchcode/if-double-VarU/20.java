public String[] getVaraNamn(String kategoriNamn) {
int kategoriID = 0;
for (int i = 0; i < kategoriList.size(); i++) {
if (kategoriList.get(i).getNamn().equals(kategoriNamn)) {
for (int i = 0; i < varaList.size(); i++) {
if (varaList.get(i).getVaruID() == varaID) {
varaDetaljer[0] = varaList.get(i).getNamn();

