            return new SingleFloat((float)Math.cos(SingleFloat.coerceToFloat(arg).value));
        if (arg instanceof Complex) {
            return new SingleFloat((float)Math.sin(SingleFloat.coerceToFloat(arg).value));
    // \"If the result of any computation would be a complex number whose
    {
        if (result instanceof Complex
            && ! (arg instanceof Complex)) {
        if (!(arg instanceof DoubleFloat)) {
    // Implementation of section 12.1.5.3, which says:
            if (arg instanceof Complex &&
                    ((Complex)arg).getRealPart() instanceof DoubleFloat) {
        if (arg instanceof Complex) {

