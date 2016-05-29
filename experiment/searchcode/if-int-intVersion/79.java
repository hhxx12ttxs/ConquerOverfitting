String driverVersion = connection.getMetaData().getDriverVersion();
String[] parts = StringUtils.split(driverVersion, &quot;.&quot;);
int intVersion = Integer.parseInt(parts[0]) * 100 + Integer.parseInt(parts[1]);
if (intVersion < 1102) {
throw MessageException.of(String.format(

