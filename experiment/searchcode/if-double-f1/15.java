float f1 = ((float)j + par3) / 43200.0F - 0.25F;

if (f1 < 0.0F)
{
++f1;
}

if (f1 > 1.0F)
{
--f1;
}

float f2 = f1;
f1 = 1.0F - (float)((Math.cos((double)f1 * Math.PI) + 1.0D) / 2.0D);

