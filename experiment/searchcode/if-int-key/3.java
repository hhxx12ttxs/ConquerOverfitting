class KeyDungeonDiv2
{
public static void main(String[] args)
{
int[] r = {4,5,4,6,8};
int[] g = {2,1,2,3,5};
System.out.println(countDoors(r,g,keys));
}
public static int countDoors(int red[],int green[],int key[])
{
int count =0;
for(int i=0;i<red.length;i++)

