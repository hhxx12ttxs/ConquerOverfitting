package gov.va.vba.vbms.cdm.converters;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
public static String printLocalDate(LocalDate localDate) {
if( localDate != null ){
return ISODateTimeFormat.date().print(localDate);

