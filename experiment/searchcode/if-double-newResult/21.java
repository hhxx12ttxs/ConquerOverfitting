public Account exchange(Account account, CurrencyRatio currencyRatio) {
if (account == null || currencyRatio == null) {
throw new IllegalArgumentException(&quot;Incorrect input parameters.&quot;);
throw new IllegalArgumentException(&quot;Bank  does not convert FROM &quot; + currencyRatio.getInitial());
}

double newResult = account.getValue() / currencyRatio.getRatio();

