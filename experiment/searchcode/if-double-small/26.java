return resultInt.doubleValue();
}

private int hcf(int i,int j)
{
int big=0;
int small=0;
if(i>j)
{
big=i;
small=j;
} else
{
big=j;
small=i;
}

return (big%small)==0?small:hcf(small,big%small);
}

}

