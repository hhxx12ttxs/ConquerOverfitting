DateAndNumberDTO other = (DateAndNumberDTO) obj;
if (localDate == null) {
if (other.localDate != null)
return false;
} else if (!localDate.equals(other.localDate))
return false;

