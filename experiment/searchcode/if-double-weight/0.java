package com.nuuxavvi.PageObjects;

public class Ball
{
private double weight = 0.0;

public Ball(double weight)
return this.weight;
}

public void setWeight(double weight)
{
if (weight < 0) {
this.weight = 0.0;
} else {
this.weight = weight;
}
}
}

