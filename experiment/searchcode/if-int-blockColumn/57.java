board[y * WIDTH + i] = level;
}
}
}

void blockColumn(int x, int y, int level)
{
for (int i = 0; i < HEIGHT; i++)
{
if (i != y &amp;&amp; board[i * WIDTH + x] == 0)

