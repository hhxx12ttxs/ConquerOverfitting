@Test(dataProvider=&quot;transitions&quot;)
public void test_transitions(JapaneseEra era, int yearOfEra, int month, int dayOfMonth, int gregorianYear) {
assertEquals(JAPANESE.prolepticYear(era, yearOfEra), gregorianYear);

