for (int i = 0; i < a.size() - 1; i++)
{
int minPos = minimumPosition(i);
swap(minPos, i);
}
}

private int minimumPosition(int from)

