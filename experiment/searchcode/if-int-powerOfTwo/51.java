int inputInt = input.nextInt();

// Initialize values
int powerOfTwo = 1;
long binaryPlace = 1;

// Find minimum power of 2 needed for this input.
while (inputInt >= powerOfTwo * 2) {
powerOfTwo *= 2;
binaryPlace *= 10;

