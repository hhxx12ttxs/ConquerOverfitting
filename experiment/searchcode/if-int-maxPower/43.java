this.character = value;
}

public double getMaxPower()
{
double maxPower = 0;
for(int i = 0; i < buffer.length; i++)
{
if(buffer[i]*buffer[i] > maxPower)
{
maxPower = buffer[i]*buffer[i];
}
}
return maxPower;
}

}

