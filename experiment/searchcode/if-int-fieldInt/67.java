public void writeToParcel(Parcel dest, int flags) {
int[] fieldInt = new int[9];
int counter = 0;
for (int i = 0; i < 3; i++)
for (int j = 0; j < 3; j++)
fieldInt[counter++] = (get_Field(j, i) == Mark.empty) ? 0 : (get_Field(j, i) == Mark.nought) ? -1 : 1;

