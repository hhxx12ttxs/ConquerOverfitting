for (int i = 0; i < a.size() - 1; i++)
{
int minPos = minPosition(i);
swap(minPos, i);
}
}


private int minPosition(int from)
{
int minPos = from;
for (int i = from + 1; i < a.size(); i++)

