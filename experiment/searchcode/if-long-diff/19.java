long thenMs = then.getTimeInMillis();

// Calculate difference in milliseconds
long diff = nowMs - thenMs;
long diffHours = diff / (60 * 60 * 1000);
long diffDays = diff / (24 * 60 * 60 * 1000);

if (diffMinutes<60){

