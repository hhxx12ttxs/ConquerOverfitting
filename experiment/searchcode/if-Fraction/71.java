numerator = numerator / 7;
denominator = denominator / 7;
}
}
}

public Fraction add(Fraction aFraction)
{
if (denominator == aFraction.denominator)
aFraction.reduce();
}
if (denominator != aFraction.denominator)
{
numerator = (denominator * aFraction.numerator) + (aFraction.denominator * numerator);

