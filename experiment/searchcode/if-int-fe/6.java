fe_sub.fe_sub(check, vxx, u);    /* vx^2-u */
if (fe_isnonzero.fe_isnonzero(check) != 0) {
fe_add.fe_add(check, vxx, u);  /* vx^2+u */
if (fe_isnonzero.fe_isnonzero(check) != 0) return -1;

