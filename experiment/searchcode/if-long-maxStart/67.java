// update the start and end indices of the max sub array
if (tmpSum > maxSum) {
maxSum = tmpSum;
maxStart = i;
tmpSum += a[i];
if (tmpSum > leftSum) {
leftSum = tmpSum;
maxStart = i;
}
}

int rightSum = Integer.MIN_VALUE;

