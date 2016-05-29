package psiborg.fractal.generators;

import psiborg.fractal.MutableComplexDouble;

public class MandelbrotGenerator implements FractalGenerator {
while (steps < THRESHOLD_STEPS) {
if (z.norm2() > 16.0) {
return steps;
}

z.sqrplusc(c);
steps++;
}

return -1;
}
}

