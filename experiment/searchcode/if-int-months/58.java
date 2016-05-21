    private static final class MulInteger extends Exec {
        static final MulInteger INSTANCE = new MulInteger();
        public XInteger eval(DynamicContext dynEnv, Item v1, Item v2) throws XQueryException {
            long i1 = asLong(v1, dynEnv);
            final long res = multiplyL(i1, i2);
            return XInteger.valueOf(res);
        }
                int months = dur1.totalMonths();
                long ym = Math.round(months * d2);
        final Exec exec;
        if(TypeUtil.subtypeOf(retType, IntegerType.INTEGER)) {
            exec = MulInteger.INSTANCE;
            if(dt1 == TypeTable.YEAR_MONTH_DURATION_TID) {

