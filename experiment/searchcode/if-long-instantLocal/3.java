final String formattedDate;
if (instantLocal.isAfter(todayMidnightLocal)) {
// Use &quot;Today&quot;
formattedDate = Languages.safeText(MessageKey.TODAY) + &quot; &quot; + Dates.formatShortTimeLocal(instantLocal);
} else if (instantLocal.isAfter(yesterdayMidnightLocal)) {
// Use &quot;Yesterday&quot;

