List<TaxBracket> mTaxBracketList;

public void addBracket(TaxBracket taxBracket) {
if (mTaxBracketList == null) {
for (TaxBracket r : mTaxBracketList) {
if (r.intersects(taxBracket)) {
throw new IllegalArgumentException(&quot;Ranges cannot intersect&quot;);

