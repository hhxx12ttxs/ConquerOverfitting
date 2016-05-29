public class Solution
{
public String convert (String s, int nRows)
{
if (nRows == 0)
return &quot;&quot;;
if (nRows == 1)
return s;
StringBuilder sb = new StringBuilder();
for (int row = 0; row < nRows; ++row)

