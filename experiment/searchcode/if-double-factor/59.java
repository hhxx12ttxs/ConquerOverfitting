import com.blogspot.toomuchcoding.person.Person;

public class TaxFactorFetcher {

static final double NO_COUNTRY_TAX_FACTOR = 14;
static final double DB_TAX_FACTOR = 8;

public double getTaxFactorFor(Person person) {
if (person.isCountryDefined()) {

