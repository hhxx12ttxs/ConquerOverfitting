public Years(YearsPK yearsPK) {
this.yearsPK = yearsPK;
}

public Years(int idSamples, int year) {
@Override
public int hashCode() {
int hash = 0;
hash += (yearsPK != null ? yearsPK.hashCode() : 0);

