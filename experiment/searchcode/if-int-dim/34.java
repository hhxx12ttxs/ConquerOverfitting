public static void main(String[] args) {

int dim = 5;
int count = 0;
int[][] array = new int [dim][dim];

for (int j = 0; j < dim; j++) {
for (int i = 0; i < dim; i++) {
if (count == i || count == dim - i - 1)

