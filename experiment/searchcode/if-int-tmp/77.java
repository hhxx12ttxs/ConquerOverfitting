for (int j = 4; j >= 1; j >>= 1)
{
int[] tmp = new int[4];
System.arraycopy(M[1][j + j], 0, tmp, 0, 4);

GCMUtil.multiplyP(tmp);
M[1][j] = tmp;
}

{
int[] tmp = new int[4];

