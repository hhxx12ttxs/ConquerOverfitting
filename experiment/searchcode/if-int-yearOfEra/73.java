* The Japanese imperial calendar year of this date.
*/
private transient int yearOfEra;

/**
* The first day supported by the JapaneseChronology is Meiji 6, January 1st.
public static JapaneseDate of(JapaneseEra era, int yearOfEra, int month, int dayOfMonth) {
Objects.requireNonNull(era, &quot;era&quot;);

