boolean[][] memo = new boolean[str.length()][str.length()];
int maxStart = 0;
int maxLength = 1;
for (int startIndex = 0; startIndex < str.length(); ++startIndex)
{
memo[startIndex][startIndex] = true;
}
for (int startIndex = 0; startIndex < str.length() - 1; ++startIndex)

