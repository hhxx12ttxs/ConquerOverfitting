        if(numeric instanceof Complex) {
            return valueof((Complex)numeric);
    public Numeric valueof(Numeric numeric) {
    public Numeric multiply(Numeric numeric) {
        if(numeric instanceof Complex) {
            return multiply((Complex)numeric);
    public Numeric add(Numeric numeric) {
        if(numeric instanceof Complex) {
            return add((Complex)numeric);
    public Numeric divide(Numeric numeric) throws ArithmeticException {
        if(numeric instanceof Complex) {
            return divide((Complex)numeric);
    public Numeric subtract(Numeric numeric) {
        if(numeric instanceof Complex) {
            return subtract((Complex)numeric);

