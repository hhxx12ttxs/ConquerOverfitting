for(int row = 0 ; row < 9 ; row++)
{
for(int col = 0 ; col < 9 ; col++)
{
num[a[row][col]] +=1 ;
if(num[a[row][col]] > 1)
num[a[row][col]] += 1 ;
if(num[a[row][col]] > 1)
{
return(&quot;no&quot;) ;
}
}
for(int i = 1 ; i < 10 ; i++)

