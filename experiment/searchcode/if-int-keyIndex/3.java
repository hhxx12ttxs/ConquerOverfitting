for(int i = 0; i < input.length - 1; i++ ) {
int keyIndex = i;

for(int j = i + 1; j < input.length; j++) {
if(input[j] < input[keyIndex])
keyIndex = j;
}

int temp = input[i];
input[i] = input[keyIndex];

