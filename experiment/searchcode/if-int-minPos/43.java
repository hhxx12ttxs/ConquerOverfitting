this.input=input;
}
public void sort()
{
for(int i=0;i<input.length-1;i++)
{
//int min=input[i];
int minPos=i;
for(int j=i+1;j<input.length;j++)
{
if (input[j]<input[minPos])
{
//min=input[j];

