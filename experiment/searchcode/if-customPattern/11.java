String dateFormatProviderClassName = parameters.getValue(DATE_FORMAT_PROVIDER_CLASS, null);

if (dateFormatProviderClassName == null) {
String customPattern = parameters.getValue(DATE_FORMAT_CUSTOM_PATTERN, DEFAULT_DATE_FORMAT_CUSTOM_PATTERN);
String numberFormatProviderClassName = parameters.getValue(NUMBER_FORMAT_PROVIDER_CLASS, null);

if (numberFormatProviderClassName == null) {
String customPattern =
parameters.getValue(NUMBER_FORMAT_CUSTOM_PATTERN, DEFAULT_NUMBER_FORMAT_CUSTOM_PATTERN);

