if (o == null || getClass() != o.getClass()) return false;

TaskCalendar that = (TaskCalendar) o;

if (weeks != null ? !weeks.equals(that.weeks) : that.weeks != null) return false;
return true;
}

@Override
public int hashCode() {
return weeks != null ? weeks.hashCode() : 0;

