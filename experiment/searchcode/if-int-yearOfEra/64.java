int era = (epochDays >= 0 ? epochDays : epochDays - 146096) / 146097;
int dayOfEra = epochDays - era * 146097;
int yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365;
int y = yearOfEra + era * 400;

