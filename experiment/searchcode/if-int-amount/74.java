return compareTo(otherAmount) == -1;
}


public String print()
{
return amount.toString();
}


@Override
public int hashCode()
{
final int prime = 31;
int result = 1;
result = prime * result + ((amount == null) ? 0 : amount.hashCode());

