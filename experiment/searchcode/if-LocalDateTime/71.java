private static final long serialVersionUID = 3505707535773076376L;

@Override
public LocalDateTime from(Timestamp timestamp) {
if (timestamp == null) {
return null;
public Timestamp to(LocalDateTime dateTime) {
if (dateTime == null) {
return null;
}
return Timestamp.valueOf(dateTime);

