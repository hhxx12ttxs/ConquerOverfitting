return 0;
}

public synchronized int MoveP2() throws InterruptedException
{
if(flag == true)
{
return 1;
}

int temp = random.nextInt(8);


while(temp == P2_Prev)
{
if(temp != P2_Prev)

