return false;
YearCategory other = (YearCategory) obj;
if (fromYear == null) {
if (other.fromYear != null)
return false;
} else if (!fromYear.equals(other.fromYear))
return false;

