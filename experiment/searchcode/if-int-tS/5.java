int c = (col / ts.tamano_caja) * ts.tamano_caja;

for (int i = 0; i < ts.tamano; i++) {
if (ts.getCelda(fila, i) == num ||
int sigCol = (col + 1) % ts.tamano;
int sigFila = (sigCol == 0) ? fila + 1 : fila;

try {
if (ts.getCelda(fila, col) != ts.VACIO)

