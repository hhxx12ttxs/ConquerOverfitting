char prev = 0;

for (int i = 0; i < l1.length; i++) {

if (l1[i] != prev) {
for (int j = 0; j < Math.min(countOcurrances(l1, l1[i]),
private static int countOcurrances(char[] l1, char c) {

int o = 0;

for (int i = 0; i < l1.length; i++) {
if (l1[i] == c) {
o++;
}
}

return o;
}

}

