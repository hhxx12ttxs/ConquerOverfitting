System.out.println(result);
}

public String longestPalindromeDP(String s)
{
int strLen = s.length();
int longestBegin = 0;
for (int i = 0; i < strLen - len + 1; i++)
{
int j = i + len - 1;
if (strChar[i] == strChar[j] &amp;&amp; palindromeFlag[i + 1][j - 1])

