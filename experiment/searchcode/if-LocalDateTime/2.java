public LocalDateTime convertTo(LocalDateTime source, LocalDateTime destination) {
if (source == null) {
return null;
}
return LocalDateTime.from(source);
public LocalDateTime convertFrom(LocalDateTime source, LocalDateTime destination) {
if (source == null) {
return null;
}
return LocalDateTime.from(source);
}

}

