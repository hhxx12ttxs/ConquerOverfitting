public Timestamp convertToDatabaseColumn( LocalDateTime localDateTime )
{
Timestamp timestamp = null;

if ( localDateTime != null )
{
Instant instant = localDateTime.atZone( ZoneId.systemDefault() ).toInstant();
LocalDateTime localDateTime = null;

if ( timestamp != null )
{
Instant instant = Instant.ofEpochMilli( timestamp.getTime() );

