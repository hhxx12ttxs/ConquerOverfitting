if (date == 0)
return &quot;Brak informacji&quot;;

long now = new Date().getTime();
long diff = now - date;

if (diff < 0)
return resolvePastDate(date, diff);
}

private static String resolvePastDate(long date, long diff) {
diff = diff / 1000;
if (diff == 1 || diff == 0)

