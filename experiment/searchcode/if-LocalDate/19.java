package org.transgalactica.management.data.people.bo.validation;

import java.time.LocalDate;
public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
if (value == null) {
return true;
}
LocalDate date = LocalDate.from(value);

