@Override
public LocalDate convertTo(final LocalDate source, final LocalDate destination) {
if (source == null) {
public LocalDate convertFrom(final LocalDate source, final LocalDate destination) {
if (source == null) {
return null;
}
return new LocalDate(source);
}

}

