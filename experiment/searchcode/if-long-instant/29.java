iMax = field.getMaximumValue() + offset;
}
else
{
iMax = maxValue;
}
}

public long add(long instant, int amount) {
FieldUtils.verifyValueBounds(this, get(instant), iMin, iMax);
return instant;
}
public long add(long instant, long amount) {

