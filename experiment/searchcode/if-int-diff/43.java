for (int i = 0; i < 4; i++)
{
a[i] = Integer.parseInt(st.nextToken().trim());
}

int diffX = a[2] - a[0];
int diffY = a[3] - a[1];
int sum = Math.abs(diffX) + Math.abs(diffY);
trollsums[A] = sum-0.6;

