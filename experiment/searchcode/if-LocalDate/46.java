package schedule.service;

import java.time.LocalDate;

import org.springframework.core.convert.converter.Converter;
public class LocalDateConverter implements Converter<String, LocalDate> {

public LocalDate convert(String text) {
if (&quot;&quot;.equals(text)) return null;
else {

