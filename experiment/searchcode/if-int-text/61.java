prefix[i] = 0;
i++;
}

}
}

public void Search(int Text[], int Patt[])
{
int n = Text.length - 1;
int i = 0;
int j = 0;
constructPrefix(Patt, Patt.length - 1);
while (i < n)
{
if (Text[i] == Patt[j])

