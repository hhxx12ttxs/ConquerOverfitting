if (concurrentNum == 0) return 0;

if (fileSizeMB <= 0.0009765625) {
double p1 = 0.001577;
double p2 = 0.03368;
return (p1 * concurrentNum + p2);//          f(x) = p1*x + p2
//       p1 =    0.001577  (0.001283, 0.001872)
//       p2 =     0.03368  (0.01824, 0.04912)
} else if (fileSizeMB <= 1) {
double p1 = 0.002026;

