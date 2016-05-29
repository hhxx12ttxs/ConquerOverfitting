int total = x * y;


int i = 0;
int j = 0;

int rightStep = x - 1;
int leftStep = x - 1;
int upStep = y - 1;
int downGap = 0;

for (int c = 0; c < total; c++) {
result.add(matrix[i][j]);
if (rightStep > 0) {

